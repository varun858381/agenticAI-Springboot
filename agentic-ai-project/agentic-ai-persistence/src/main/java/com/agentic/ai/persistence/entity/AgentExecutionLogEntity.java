package com.agentic.ai.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agent_execution_logs")
public class AgentExecutionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false, length = 100)
    private String agentName;

    private int iterationNumber;

    @Column(columnDefinition = "TEXT")
    private String inputPrompt;

    @Column(columnDefinition = "TEXT")
    private String output;

    private long durationMs;
    private boolean success;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @PrePersist
    public void prePersist() { this.executedAt = LocalDateTime.now(); }
}
