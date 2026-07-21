<!-- CODEGRAPH_START -->
## CodeGraph

This repo has a `.codegraph/` index. Use `codegraph_explore` (MCP) or `codegraph explore "<query>"` (shell) to find and read symbols before grep/read. It returns verbatim source + call paths. One call usually replaces a grep+read loop.
<!-- CODEGRAPH_END -->

## Project: IReader

Kotlin Multiplatform reader app for light/web novels and ebooks. Targets Android, Desktop (JVM), and iOS.

### Quick Facts

| Key | Value |
|-----|-------|
| Language | Kotlin 2.3.21 (K2 compiler) |
| UI | Compose Multiplatform + Material Design 3 |
| Architecture | Clean Architecture (domain → data → presentation) |
| DI | Koin 4.2.1 |
| HTTP | Ktor 3.5.0 |
| Database | SQLDelight 2.3.2 |
| HTML Parser | Ksoup 0.2.6 (KMP Jsoup) |
| Backend | Supabase 3.6.0 |
| Testing | kotlin.test + kotlinx-coroutines-test + mockk |
| Build | Gradle with version catalogs |
| Static Analysis | detekt 1.23.8 |

### Module Structure

```
domain/    — Entities (Book, Chapter, History), use cases, repository interfaces
data/      — SQLDelight DB, Ktor/Supabase API, repository implementations
presentation/ — Compose Multiplatform screens, ViewModels, navigation
presentation-core/ — Shared theme (IReaderTheme), scaffold, components
core/      — IO, HTTP, config, preferences, DB utilities, logging
source-api/ — Published extension API (Source, CatalogSource, HttpSource)
plugin-api/ — Plugin interface definitions
source-runtime-js/ — JS engine for user sources
i18n/      — Internationalization
android/   — Android app entry
desktop/   — Desktop app entry
iosApp/    — iOS Xcode project
```

### Key Commands

```bash
./gradlew test                        # Full test suite
./gradlew :domain:test                # Domain module tests
./gradlew :presentation:testDebugUnitTest  # Presentation tests
./gradlew detekt                      # Static analysis
./gradlew :desktop:run                # Run desktop app
```

### Agent Rules

1. **TDD is mandatory** — write failing test before any implementation code. Use the `tdd` skill.
2. **Clean Architecture** — domain never imports data or presentation. Use `ireader-architecture` skill.
3. **Vertical slices** — one failing test → implementation → pass → next. Not horizontal (all tests first).
4. **Memory** — persist project knowledge in `.opencode/memory/`. Use `ireader-memory` skill.
5. **Skills first** — check `.opencode/skills/` before writing code. The `ask-matt` skill helps route.
6. **Ponytail philosophy** — minimal code, YAGNI, stdlib first. The `ponytail` skill enforces this.
7. **No completion without verification** — run the command, read the output, then claim. Use `get-shit-done`.

### Available Skills (`.opencode/skills/`)

**Universal (85 skills from skill-tree):**
Architecture, backend, DevOps, security, testing, API, Git, workflow, productivity, and more.

**IReader-specific:**
- `ireader-kmp` — KMP feature development with module structure, test commands, entity model
- `ireader-source` — Novel source extension development (SimpleNovelSource, HttpSource)
- `ireader-compose` — Compose UI development (theme, ViewModels, testing)
- `ireader-architecture` — Clean Architecture enforcement (layer boundaries, DI, patterns)
- `ireader-debugging` — Debugging process for common IReader failures
- `ireader-build` — Gradle build commands, config, troubleshooting
- `ireader-memory` — Cross-session memory management

### Session Workflow

1. **Start**: Load `ireader-memory` → read MEMORY.md → read last session notes
2. **Task**: Check skills → `ask-matt` for routing → load relevant skill
3. **Implement**: TDD cycle (red → green → refactor) following the skill
4. **Verify**: Run tests, check output, verify claim
5. **End**: Save session notes, update MEMORY.md with new facts

### Quick Reference: Source Extensions

| Approach | Use Case |
|----------|----------|
| `SimpleNovelSource` | Most HTML sites (recommended) |
| `HttpSource` | Advanced site control |
| DSL Builder | Rapid prototyping |

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
