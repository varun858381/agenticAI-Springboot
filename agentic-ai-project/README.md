# Agentic AI — Spring Boot Multi-Module Microservice

A production-ready **Maven multi-module** Spring Boot microservice implementing
the Agentic AI pattern: an LLM-powered loop that **Plans → Delegates → Executes → Reflects**
until a goal is achieved, with full **PostgreSQL persistence** at every step.

---

## Module Structure

```
agentic-ai-parent/                  ← Root POM (dependency management)
├── agentic-ai-core/                ← Interfaces, DTOs (Agent, AgentTaskRequest, AgentResult)
├── agentic-ai-persistence/         ← JPA Entities + Spring Data Repositories
├── agentic-ai-agents/              ← Agent implementations (WebSearch, DataAnalysis, CodeGen, Email)
├── agentic-ai-orchestrator/        ← Agentic loop service + auto-registrar
└── agentic-ai-api/                 ← Spring Boot app: REST controllers, config, Flyway migrations
```

### Dependency Graph

```
agentic-ai-core
      ↑
agentic-ai-persistence
      ↑
agentic-ai-agents
      ↑
agentic-ai-orchestrator
      ↑
agentic-ai-api  ←  (runnable fat JAR)
```

---

## Prerequisites

| Tool        | Version   |
|-------------|-----------|
| Java        | 21+       |
| Maven       | 3.9+      |
| PostgreSQL  | 15+       |
| OpenAI key  | Any tier  |

---

## Quick Start

### 1. Start PostgreSQL

```bash
docker run -d \
  --name agentdb \
  -e POSTGRES_DB=agentdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16
```

### 2. Set environment variables

```bash
export OPENAI_API_KEY=sk-...
```

### 3. Build all modules

```bash
mvn clean install -DskipTests
```

### 4. Run the application

```bash
cd agentic-ai-api
mvn spring-boot:run
```

Flyway automatically creates all 4 tables and seeds the 4 default agents on first run.

---

## REST API

### Run an agentic loop

```bash
curl -X POST http://localhost:8080/api/agents/run \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Research the top 3 AI trends in 2025 and write a summary",
    "maxIterations": 5
  }'
```

**Response:**
```json
{
  "taskId": 1,
  "finalOutput": "...",
  "goalAchieved": true,
  "iterationsUsed": 2,
  "memoryTrace": "[WebSearchAgent #1]: ..."
}
```

### Other endpoints

| Method | Endpoint                             | Description                    |
|--------|--------------------------------------|--------------------------------|
| GET    | `/api/agents`                        | List all registered agents     |
| GET    | `/api/agents/{name}`                 | Get agent by name              |
| GET    | `/api/agents/tasks`                  | List all tasks                 |
| GET    | `/api/agents/tasks/status/{status}`  | Filter tasks by status         |
| GET    | `/api/agents/tasks/{id}/result`      | Get result for a task          |
| GET    | `/api/agents/tasks/{id}/logs`        | Get execution audit log        |
| GET    | `/actuator/health`                   | Health check                   |

---

## Database Schema

| Table                  | Purpose                                      |
|------------------------|----------------------------------------------|
| `agents`               | Registry of all agents (seeded at startup)   |
| `agent_tasks`          | One row per agentic loop run                 |
| `agent_results`        | Final output + success flag per task         |
| `agent_execution_logs` | Full audit trail: every agent call logged    |

Schema is managed by **Flyway** (`V1__init_agent_schema.sql`).

---

## Adding a Custom Agent

1. Create a class in `agentic-ai-agents` implementing `Agent`:

```java
@Component
@RequiredArgsConstructor
public class MyDatabaseAgent implements Agent {

    @Override public String getName() { return "MyDatabaseAgent"; }
    @Override public String getDescription() { return "Queries internal databases"; }
    @Override public List<String> getCapabilities() {
        return List.of("database", "query", "sql");
    }

    @Override
    public String execute(String task) {
        // your logic — call a DB, call an API, etc.
        return result;
    }
}
```

2. That's it. `AgentAutoRegistrar` picks up every `@Component` implementing `Agent`
   on startup, registers it with the orchestrator, and persists it to the `agents` table.

---

## Running Tests

```bash
# All tests (uses H2 in-memory DB via 'test' profile)
mvn test

# Single module
mvn test -pl agentic-ai-api
```

---

## Configuration Reference

| Property                            | Default       | Description              |
|-------------------------------------|---------------|--------------------------|
| `spring.datasource.url`             | localhost:5432 | PostgreSQL URL          |
| `spring.ai.openai.api-key`          | `$OPENAI_API_KEY` | OpenAI API key       |
| `spring.ai.openai.chat.options.model` | `gpt-4o`    | LLM model               |
| `spring.ai.openai.chat.options.temperature` | `0.3` | Sampling temperature  |

---

## Task Status Flow

```
RUNNING → COMPLETED   (goal achieved within maxIterations)
        → PARTIAL     (loop ended, goal not fully achieved)
        → FAILED      (exception thrown)
```
