package com.agentic.ai.orchestrator;

import com.agentic.ai.core.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Automatically registers all Agent beans with the orchestrator
 * and the database when the Spring context is fully started.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentAutoRegistrar {

    private final ApplicationContext context;
    private final AgentOrchestrator orchestrator;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Map<String, Agent> agents = context.getBeansOfType(Agent.class);
        log.info("Auto-registering {} agent(s)...", agents.size());
        agents.values().forEach(agent -> {
            orchestrator.registerAgent(agent);
            log.info("  ✓ {}", agent.getName());
        });
        log.info("All agents registered and persisted to DB.");
    }
}
