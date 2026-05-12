package com.agentic.ai.core;

import java.util.List;

/**
 * Core contract for every Agentic AI agent.
 * Implement this interface and annotate with @Component
 * to auto-register the agent at startup.
 */
public interface Agent {

    /** Unique agent identifier used by the orchestrator. */
    String getName();

    /** Human-readable description of what this agent does. */
    String getDescription();

    /**
     * List of capabilities / keywords used by the planner
     * to select the right agent for a given task.
     * Examples: "search", "analyze", "generate-code", "email"
     */
    List<String> getCapabilities();

    /**
     * Execute the given task string and return a result.
     *
     * @param task  Natural-language instruction from the planner.
     * @return      String result to be appended to agent memory.
     */
    String execute(String task);
}
