package com.agentic.ai.persistence.repository;

import com.agentic.ai.persistence.entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, Long> {
    Optional<AgentEntity> findByName(String name);
    List<AgentEntity> findByStatus(String status);
    boolean existsByName(String name);
}
