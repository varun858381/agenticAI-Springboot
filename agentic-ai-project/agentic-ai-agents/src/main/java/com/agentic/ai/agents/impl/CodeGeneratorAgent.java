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
 * Generates, reviews, and refactors code.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeGeneratorAgent implements Agent {

    private final ChatClient chatClient;

    @Override public String getName() { return "CodeGeneratorAgent"; }
    @Override public String getDescription() { return "Generates, reviews and refactors production-grade code"; }
    @Override public List<String> getCapabilities() {
        return List.of("generate-code", "refactor", "review", "debug", "test", "document");
    }

    @Override
    public String execute(String task) {
        log.info("[CodeGeneratorAgent] executing: {}", task);
        String prompt = """
            You are a senior software engineer. Generate clean, well-commented code for:
            %s
            Include: imports, error handling, and brief inline comments.
            """.formatted(task);
        return chatClient.prompt(new Prompt(new UserMessage(prompt))).call().content();
    }
}
