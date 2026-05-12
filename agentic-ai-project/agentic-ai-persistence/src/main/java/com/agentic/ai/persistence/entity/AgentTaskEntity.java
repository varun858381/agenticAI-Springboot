package com.agentic.ai.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agent_tasks")
public class AgentTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String goal;

    @Column(nullable = false, length = 20)
    private String status; // RUNNING | COMPLETED | PARTIAL | FAILED

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    public void prePersist() { this.startedAt = LocalDateTime.now(); }
}
