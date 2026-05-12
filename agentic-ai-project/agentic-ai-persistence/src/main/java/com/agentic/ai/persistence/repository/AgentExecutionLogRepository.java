package com.agentic.ai.persistence.repository;

import com.agentic.ai.persistence.entity.AgentExecutionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentExecutionLogRepository extends JpaRepository<AgentExecutionLogEntity, Long> {
    List<AgentExecutionLogEntity> findByTaskIdOrderByIterationNumberAsc(Long taskId);
    List<AgentExecutionLogEntity> findByAgentName(String agentName);

    @Query("SELECT AVG(l.durationMs) FROM AgentExecutionLogEntity l WHERE l.agentName = :agentName")
    Double avgDurationByAgent(String agentName);

    @Query("SELECT COUNT(l) FROM AgentExecutionLogEntity l WHERE l.agentName = :agentName AND l.success = true")
    long countSuccessByAgent(String agentName);
}
