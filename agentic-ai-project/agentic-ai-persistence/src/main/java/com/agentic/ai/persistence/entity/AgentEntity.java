package com.agentic.ai.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String capabilities;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE | INACTIVE | ERROR

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
