-- =============================================================
-- V1__init_agent_schema.sql
-- Flyway migration: create all tables for the Agentic AI system
-- =============================================================

-- 1. Agents registry ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS agents (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    description  TEXT,
    capabilities TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);

-- 2. Agent tasks (one row per agentic-loop invocation) ────────
CREATE TABLE IF NOT EXISTS agent_tasks (
    id            BIGSERIAL   PRIMARY KEY,
    goal          TEXT        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    started_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at  TIMESTAMP,
    error_message TEXT
);

-- 3. Agent results (final output per task) ────────────────────
CREATE TABLE IF NOT EXISTS agent_results (
    id            BIGSERIAL  PRIMARY KEY,
    task_id       BIGINT     NOT NULL REFERENCES agent_tasks(id) ON DELETE CASCADE,
    output        TEXT,
    goal_achieved BOOLEAN    NOT NULL DEFAULT FALSE,
    iterations    INT        NOT NULL DEFAULT 0,
    memory_trace  TEXT,
    completed_at  TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- 4. Full execution audit log ─────────────────────────────────
CREATE TABLE IF NOT EXISTS agent_execution_logs (
    id               BIGSERIAL    PRIMARY KEY,
    task_id          BIGINT       NOT NULL REFERENCES agent_tasks(id) ON DELETE CASCADE,
    agent_name       VARCHAR(100) NOT NULL,
    iteration_number INT          NOT NULL,
    input_prompt     TEXT,
    output           TEXT,
    duration_ms      BIGINT,
    success          BOOLEAN      NOT NULL DEFAULT TRUE,
    executed_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes ─────────────────────────────────────────────────────
CREATE INDEX idx_agent_tasks_status   ON agent_tasks(status);
CREATE INDEX idx_agent_results_task   ON agent_results(task_id);
CREATE INDEX idx_exec_logs_task       ON agent_execution_logs(task_id);
CREATE INDEX idx_exec_logs_agent      ON agent_execution_logs(agent_name);

-- Seed: pre-register known agents ─────────────────────────────
INSERT INTO agents (name, description, capabilities, status) VALUES
    ('WebSearchAgent',
     'Searches the web for up-to-date information',
     'search,lookup,fetch,browse,research',
     'ACTIVE'),
    ('DataAnalysisAgent',
     'Analyzes data, generates reports and draws business insights',
     'analyze,report,summarize,compare,aggregate,insights',
     'ACTIVE'),
    ('CodeGeneratorAgent',
     'Generates, reviews and refactors production-grade code',
     'generate-code,refactor,review,debug,test,document',
     'ACTIVE'),
    ('EmailNotificationAgent',
     'Drafts and sends email notifications',
     'email,notify,send,draft,communicate',
     'ACTIVE')
ON CONFLICT (name) DO NOTHING;
