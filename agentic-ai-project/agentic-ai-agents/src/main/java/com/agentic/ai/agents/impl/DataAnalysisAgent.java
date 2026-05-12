package com.agentic.ai.agents.impl;

import com.agentic.ai.core.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Analyzes data and produces structured reports and insights.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataAnalysisAgent implements Agent {

    private final ChatClient chatClient;

    @Override public String getName() { return "DataAnalysisAgent"; }
    @Override public String getDescription() { return "Analyzes data, generates reports and draws business insights"; }
    @Override public List<String> getCapabilities() {
        return List.of("analyze", "report", "summarize", "compare", "aggregate", "insights");
    }

    @Override
    public String execute(String task) {
        log.info("[DataAnalysisAgent] executing: {}", task);
        String prompt = """
            You are a senior data analyst. Analyze and provide structured insights for:
            %s
            Format: Key findings, trends, and actionable recommendations.
            """.formatted(task);
        return chatClient.prompt(new Prompt(new UserMessage(prompt))).call().content();
    }
}
