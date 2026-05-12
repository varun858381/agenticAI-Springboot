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
 * Searches the web (or simulates it via LLM) for real-time information.
 * In production: wire to Tavily, SerpAPI, or Playwright.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSearchAgent implements Agent {

    private final ChatClient chatClient;

    @Override public String getName() { return "WebSearchAgent"; }
    @Override public String getDescription() { return "Searches the web for up-to-date information"; }
    @Override public List<String> getCapabilities() {
        return List.of("search", "lookup", "fetch", "browse", "research");
    }

    @Override
    public String execute(String task) {
        log.info("[WebSearchAgent] executing: {}", task);
        String prompt = """
            You are a web search assistant. Provide a concise, factual answer for:
            %s
            Format: 3-5 sentences with the most relevant information.
            """.formatted(task);
        return chatClient.prompt(new Prompt(new UserMessage(prompt))).call().content();
    }
}
