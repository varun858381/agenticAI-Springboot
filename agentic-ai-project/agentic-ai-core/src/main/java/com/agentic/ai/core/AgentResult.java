package com.agentic.ai.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after the agentic loop completes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {

    private Long taskId;
    private String finalOutput;
    private boolean goalAchieved;
    private int iterationsUsed;

    /** Full memory trace of every iteration. */
    private String memoryTrace;
}
