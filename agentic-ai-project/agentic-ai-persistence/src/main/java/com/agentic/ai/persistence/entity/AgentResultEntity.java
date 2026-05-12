package com.agentic.ai.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agent_results")
public class AgentResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(columnDefinition = "TEXT")
    private String output;

    private boolean goalAchieved;
    private int iterations;

    @Column(columnDefinition = "TEXT")
    private String memoryTrace;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() { this.completedAt = LocalDateTime.now(); }
}
