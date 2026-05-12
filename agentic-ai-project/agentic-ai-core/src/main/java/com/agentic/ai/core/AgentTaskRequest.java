package com.agentic.ai.core;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO: sent by client to start an agentic loop.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskRequest {

    @NotBlank(message = "Goal must not be blank")
    private String goal;

    @Min(value = 1, message = "maxIterations must be at least 1")
    @Builder.Default
    private int maxIterations = 5;
}
