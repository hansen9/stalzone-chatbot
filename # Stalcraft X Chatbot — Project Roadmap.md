# Stalcraft X Chatbot — Project Roadmap

## Current Status Legend
- ✅ Complete
- 🔄 In Progress
- 🔲 Not Started

---

## Phase 1 — Data Foundation ✅
| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | `pom.xml` — SB 3.3.6, Java 21, Spring AI, Lombok, WireMock | ✅ | Clean dependency set, no conflicts |
| 2 | `application.properties` — H2, OpenRouter, GitHub base URL | ✅ | Env var for API key, migration TODOs documented |
| 3 | `GameItem` — clean JPA entity, Lombok, H2-compatible columns | ✅ | No parsing logic, TEXT columns instead of jsonb |
| 4 | `InfoBlockParser` — deserialise infoBlocks, extract numeric stats | ✅ | `parseInfoBlocks()` + `extractAllNumericStats()` |
| 5 | `GithubDataFetcher` — injected RestClient, `GameDocument` record | ✅ | Base URL from properties, no hardcoded URLs |
| 6 | `GameItemMapper` — translates GameDocument → GameItem entity | ✅ | Owns iconPath derivation + stats serialisation |
| 7 | `ItemRepository` — JpaRepository interface, named query methods | ✅ | `findByCategory`, `findByNameEnIgnoreCase` |
| 8 | `DataIngestionService` — orchestrates on startup, @Transactional | ✅ | ApplicationReadyEvent, batched saveAll, TODO for Postgres skip |

---

## Phase 2 — Tests 🔄
| # | Task | Status | Notes |
|---|------|--------|-------|
| 9  | `InfoBlockParserTest` — pure unit test, real fixture (`0r2g1.json`) | ✅ | 10 cases: happy path, empty elements, null/bad input |
| 10 | `GithubDataFetcherTest` — WireMock fake HTTP server | 🔲 | Serves fixture JSON, verifies GameDocument mapping |
| 11 | `DataIngestionServiceTest` — Mockito, verifies orchestration | 🔲 | Mock fetcher/parser/mapper/repo, verify call flow |

---

## Phase 3 — AI Layer 🔲
| # | Task | Status | Notes |
|---|------|--------|-------|
| 12 | `AiConfig` — configure Spring AI `ChatClient`, system prompt | 🔲 | Stalcraft-aware system prompt, OpenRouter model |
| 13 | `ItemLookUpTool` — `@Tool` method, queries `ItemRepository` | 🔲 | First Spring AI tool — sets the pattern for all others |
| 14 | `ChatService` — wires `ChatClient` + tools, conversation memory | 🔲 | Stateless first, add memory in Phase 5 |
| 15 | `ChatController` — `POST /api/chat`, request/response DTOs | 🔲 | `ChatRequest`, `ChatResponse` |
| 16 | `WebConfig` — CORS config for Vue dev server on port 5173 | 🔲 | Needed before frontend can call backend |

---

## Phase 4 — Feature Tools 🔲
> Each tool follows the same `@Tool` pattern established in Phase 3.

| # | Task | Status | Notes |
|---|------|--------|-------|
| 17 | TTK / BTK calculator tool | 🔲 | Uses damage + rate_of_fire + armor stats from DB |
| 18 | Build suggester tool | 🔲 | Queries items by category, filters by rank/stats |
| 19 | Item comparator tool | 🔲 | Side-by-side stat diff for multiple items |
| 20 | Crafting calculator tool | 🔲 | Requires crafting recipe data from GitHub repo |
| 21 | Marketplace trends tool | 🔲 | ⚠️ Needs Stalcraft public API — not GitHub data. Research API availability + rate limits before starting. |

---

## Phase 5 — Vue Frontend 🔲
| # | Task | Status | Notes |
|---|------|--------|-------|
| 22 | Vue project scaffold — Vite + Vue 3 + Tailwind | 🔲 | Separate repo or `/frontend` subfolder |
| 23 | Chat UI component — message thread, input bar, streaming | 🔲 | Core feature |
| 24 | TTK calculator panel | 🔲 | Structured input form → formatted result |
| 25 | Item comparator panel | 🔲 | Side-by-side stat table |
| 26 | Marketplace trends panel | 🔲 | Chart/graph, depends on Phase 4 item 21 |

---

## Phase 6 — Production Readiness 🔲
| # | Task | Status | Notes |
|---|------|--------|-------|
| 27 | Swap H2 → PostgreSQL + Docker Compose | 🔲 | Add `postgresql` driver, remove H2 |
| 28 | Flyway migrations — replace `create-drop` with versioned SQL | 🔲 | `V1__create_game_item.sql` etc. |
| 29 | Spring profiles — `application-dev.properties` / `application-prod.properties` | 🔲 | Move show-sql, H2 console to dev profile |
| 30 | `GameItem` stats column — migrate TEXT → `jsonb` with `@JdbcTypeCode` | 🔲 | TODOs already marked in GameItem + GameItemMapper |
| 31 | `VectorStoreService` + pgvector — semantic item search for RAG | 🔲 | Requires Postgres. Enables "find me an armor like X" queries |

---

## Key Technical Decisions Log
| Decision | Choice | Reason |
|----------|--------|--------|
| Spring Boot version | 3.3.6 | Current LTS, best Spring AI compatibility |
| Java version | 21 | Current LTS, records + virtual threads |
| AI provider | OpenRouter (OpenAI-compatible) | Free tier mo