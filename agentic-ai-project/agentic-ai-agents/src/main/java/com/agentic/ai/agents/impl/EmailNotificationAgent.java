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
 * Drafts and sends email notifications.
 * In production: wire to JavaMailSender or SendGrid.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAgent implements Agent {

    private final ChatClient chatClient;

    @Override public String getName() { return "EmailNotificationAgent"; }
    @Override public String getDescription() { return "Drafts and sends email notifications"; }
    @Override public List<String> getCapabilities() {
        return List.of("email", "notify", "send", "draft", "communicate");
    }

    @Override
    public String execute(String task) {
        log.info("[EmailNotificationAgent] executing: {}", task);
        String prompt = """
            You are an email drafting assistant. Draft a professional email for:
            %s
            Format: Subject line, greeting, body (2-3 paragraphs), sign-off.
            """.formatted(task);
        return chatClient.prompt(new Prompt(new UserMessage(prompt))).call().content();
    }
}
