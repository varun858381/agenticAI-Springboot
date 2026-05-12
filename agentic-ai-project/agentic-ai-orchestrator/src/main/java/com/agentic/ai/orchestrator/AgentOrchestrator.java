package com.agentic.ai.orchestrator;

import com.agentic.ai.core.Agent;
import com.agentic.ai.core.AgentResult;
import com.agentic.ai.core.AgentTaskRequest;
import com.agentic.ai.persistence.entity.*;
import com.agentic.ai.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Agentic Loop:  Plan → Delegate → Execute → Reflect → Persist
 *
 * The orchestrator is LLM-powered: it asks the model what to do next,
 * picks the matching agent, runs it, then reflects on whether the goal
 * is achieved. This loop repeats until success or maxIterations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final ChatClient chatClient;
    private final AgentRepository agentRepository;
    private final AgentTaskRepository taskRepository;
    private final AgentResultRepository resultRepository;
    private final AgentExecutionLogRepository logRepository;

    /** Live registry: name → agent bean */
    private final Map<String, Agent> agentRegistry = new ConcurrentHashMap<>();

    // ----------------------------------------------------------------
    //  Registration
    // ----------------------------------------------------------------

    /**
     * Register an agent in-memory and persist metadata to DB.
     * Called automatically at startup by AgentAutoRegistrar.
     */
    @Transactional
    public void registerAgent(Agent agent) {
        agentRegistry.put(agent.getName(), agent);

        if (!agentRepository.existsByName(agent.getName())) {
            AgentEntity entity = new AgentEntity();
            entity.setName(agent.getName());
            entity.setDescription(agent.getDescription());
            entity.setCapabilities(String.join(",", agent.getCapabilities()));
            entity.setStatus("ACTIVE");
            agentRepository.save(entity);
        }
        log.info("Registered agent: {}", agent.getName());
    }

    // ----------------------------------------------------------------
    //  Agentic Loop
    // ----------------------------------------------------------------

    @Transactional
    public AgentResult runAgenticLoop(AgentTaskRequest request) {
        log.info("Starting agentic loop | goal='{}' | maxIterations={}",
                request.getGoal(), request.getMaxIterations());

        // 1. Persist the task
        AgentTaskEntity taskEntity = new AgentTaskEntity();
        taskEntity.setGoal(request.getGoal());
        taskEntity.setStatus("RUNNING");
        taskRepository.save(taskEntity);

        StringBuilder memory = new StringBuilder();
        String lastResult = null;
        boolean goalAchieved = false;
        int iterationsUsed = 0;

        try {
            for (int i = 0; i < request.getMaxIterations() && !goalAchieved; i++) {
                iterationsUsed = i + 1;
                log.info("=== Iteration {}/{} ===", iterationsUsed, request.getMaxIterations());

                // PLAN
                String plan = plan(request.getGoal(), memory.toString(), lastResult);
                log.info("Plan: {}", plan);

                // DELEGATE
                String agentName = selectAgentName(plan);
                Agent agent = agentRegistry.get(agentName);
                if (agent == null) {
                    log.warn("Agent '{}' not found — skipping iteration", agentName);
                    continue;
                }

                // EXECUTE
                long start = System.currentTimeMillis();
                String result = agent.execute(plan);
                long durationMs = System.currentTimeMillis() - start;
                lastResult = result;
                memory.append("\n[").append(agentName).append(" #").append(iterationsUsed)
                      .append("]: ").append(result);

                // LOG execution
                persistExecutionLog(taskEntity.getId(), agentName, iterationsUsed,
                        plan, result, durationMs, true);

                // REFLECT
                goalAchieved = reflect(request.getGoal(), result);
                log.info("Goal achieved: {}", goalAchieved);
            }

            // 2. Persist result
            persistResult(taskEntity, lastResult, goalAchieved, iterationsUsed, memory.toString());

            taskEntity.setStatus(goalAchieved ? "COMPLETED" : "PARTIAL");
            taskEntity.setCompletedAt(LocalDateTime.now());
            taskRepository.save(taskEntity);

        } catch (Exception ex) {
            log.error("Agentic loop failed", ex);
            taskEntity.setStatus("FAILED");
            taskEntity.setErrorMessage(ex.getMessage());
            taskEntity.setCompletedAt(LocalDateTime.now());
            taskRepository.save(taskEntity);
            throw ex;
        }

        return AgentResult.builder()
                .taskId(taskEntity.getId())
                .finalOutput(lastResult)
                .goalAchieved(goalAchieved)
                .iterationsUsed(iterationsUsed)
                .memoryTrace(memory.toString())
                .build();
    }

    // ----------------------------------------------------------------
    //  LLM-powered helpers
    // ----------------------------------------------------------------

    private String plan(String goal, String memory, String lastResult) {
        String prompt = """
            You are an AI task planner. Choose the next best action to achieve the goal.

            Goal: %s
            Memory (previous steps): %s
            Last result: %s
            Available agents: %s

            Respond in EXACTLY this format (one line):
            AGENT:<agent_name> ACTION:<specific instruction>
            """.formatted(goal,
                memory.isBlank() ? "none" : memory,
                lastResult == null ? "none" : lastResult,
                String.join(", ", agentRegistry.keySet()));

        return chatClient.prompt(new Prompt(new UserMessage(prompt))).call().content();
    }

    private String selectAgentName(String plan) {
        if (plan != null && plan.contains("AGENT:")) {
            return plan.split("AGENT:")[1].split("\\s")[0].trim();
        }
        return agentRegistry.keySet().iterator().next();
    }

    private boolean reflect(String goal, String result) {
        String prompt = """
            Has this result fully achieved the goal?
            Goal: %s
            Result: %s
            Answer with YES or NO only. Nothing else.
            """.formatted(goal, result);

        String response = chatClient.prompt(new Prompt(new UserMessage(prompt))).call().content();
        return response != null && response.trim().toUpperCase().startsWith("YES");
    }

    // ----------------------------------------------------------------
    //  DB persistence helpers
    // ----------------------------------------------------------------

    private void persistResult(AgentTaskEntity task, String output,
                                boolean goalAchieved, int iterations, String memory) {
        AgentResultEntity result = new AgentResultEntity();
        result.setTaskId(task.getId());
        result.setOutput(output);
        result.setGoalAchieved(goalAchieved);
        result.setIterations(iterations);
        result.setMemoryTrace(memory);
        resultRepository.save(result);
    }

    private void persistExecutionLog(Long taskId, String agentName, int iteration,
                                      String input, String output, long durationMs, boolean success) {
        AgentExecutionLogEntity log = new AgentExecutionLogEntity();
        log.setTaskId(taskId);
        log.setAgentName(agentName);
        log.setIterationNumber(iteration);
        log.setInputPrompt(input);
        log.setOutput(output);
        log.setDurationMs(durationMs);
        log.setSuccess(success);
        logRepository.save(log);
    }
}
