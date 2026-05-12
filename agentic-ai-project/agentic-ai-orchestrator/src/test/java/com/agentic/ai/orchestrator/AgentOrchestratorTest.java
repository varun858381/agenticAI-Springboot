package com.agentic.ai.orchestrator;

import com.agentic.ai.core.Agent;
import com.agentic.ai.core.AgentResult;
import com.agentic.ai.core.AgentTaskRequest;
import com.agentic.ai.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentOrchestratorTest {

    @Mock ChatClient chatClient;
    @Mock ChatClient.CallResponseSpec callSpec;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock AgentRepository agentRepository;
    @Mock AgentTaskRepository taskRepository;
    @Mock AgentResultRepository resultRepository;
    @Mock AgentExecutionLogRepository logRepository;

    @InjectMocks AgentOrchestrator orchestrator;

    private Agent mockAgent;

    @BeforeEach
    void setup() {
        mockAgent = new Agent() {
            public String getName() { return "TestAgent"; }
            public String getDescription() { return "Test"; }
            public List<String> getCapabilities() { return List.of("test"); }
            public String execute(String task) { return "Test result for: " + task; }
        };
    }

    @Test
    void registerAgent_addsToRegistry() {
        when(agentRepository.existsByName("TestAgent")).thenReturn(false);
        when(agentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        orchestrator.registerAgent(mockAgent);
        // If no exception thrown, registration succeeded
    }
}
