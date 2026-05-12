package com.agentic.ai.persistence.repository;

import com.agentic.ai.persistence.entity.AgentTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTaskEntity, Long> {
    List<AgentTaskEntity> findByStatus(String status);

    @Query("SELECT t FROM AgentTaskEntity t WHERE CAST(t.startedAt AS date) = CURRENT_DATE")
    List<AgentTaskEntity> findTodayTasks();

    @Query("SELECT COUNT(t) FROM AgentTaskEntity t WHERE t.status = :status")
    long countByStatus(String status);
}
