package com.agentic.ai.persistence.repository;

import com.agentic.ai.persistence.entity.AgentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentResultRepository extends JpaRepository<AgentResultEntity, Long> {
    Optional<AgentResultEntity> findByTaskId(Long taskId);
    List<AgentResultEntity> findByGoalAchieved(boolean goalAchieved);
}
