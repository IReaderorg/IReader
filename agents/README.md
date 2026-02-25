# IReader Custom Agents

This directory contains 5 specialized AI agents designed for the IReader project. Each agent is an expert in a specific domain and follows the project's TDD methodology and Superpowers workflow.

## Available Agents

### 1. KMP Feature Developer (`kmp-feature-developer`)

**Use when:** Building new features across Android, iOS, and Desktop platforms

**Specializes in:**
- Implementing features with strict TDD (RED-GREEN-REFACTOR)
- Clean Architecture (domain → data → presentation)
- Kotlin Multiplatform expect/actual patterns
- Writing tests in commonTest first
- Platform-specific implementations

**Example usage:**
```
@kmp-feature-developer Implement a new bookmark feature that syncs across all platforms
```

### 2. Extension Source Builder (`extension-source-builder`)

**Use when:** Creating new novel source extensions or fixing existing ones

**Specializes in:**
- Analyzing website HTML structure
- Implementing Madara, SourceFactory, and custom API patterns
- Testing HTML parsing and selectors
- Using KSP annotations
- Building and testing extensions

**Example usage:**
```
@extension-source-builder Create a new source for NovelSite.com that uses Madara theme
```

### 3. Compose UI Specialist (`compose-ui-specialist`)

**Use when:** Building or modifying UI screens and components

**Specializes in:**
- Jetpack Compose UI implementation with TDD
- Material Design 3 components
- State management with ViewModels
- ComposeTestRule testing
- Accessibility and performance

**Example usage:**
```
@compose-ui-specialist Create a new reading settings screen with font size and theme controls
```

### 4. Systematic Debugger (`systematic-debugger`)

**Use when:** Encountering bugs, test failures, or unexpected behavior

**Specializes in:**
- Root cause investigation (4-phase process)
- Evidence gathering before fixing
- Creating failing tests that reproduce bugs
- Never fixing symptoms, only root causes
- Questioning architecture after 3+ failed fixes

**Example usage:**
```
@systematic-debugger The chapter list is empty but the API returns data. Find the root cause.
```

### 5. Architecture Refactoring (`architecture-refactoring`)

**Use when:** Improving code quality or refactoring architecture

**Specializes in:**
- Refactoring with test coverage
- Maintaining Clean Architecture boundaries
- Applying SOLID principles
- Removing code duplication (DRY)
- Simplifying complexity (YAGNI)

**Example usage:**
```
@architecture-refactoring Refactor the ReaderViewModel - it has too many responsibilities
```

## How to Use

### Invoke an Agent

Use the `@agent-name` syntax in your message:

```
@kmp-feature-developer Add a reading history feature
```

### Agent Selection Guide

| Task | Recommended Agent |
|------|-------------------|
| New feature implementation | `kmp-feature-developer` |
| New website source | `extension-source-builder` |
| UI screen or component | `compose-ui-specialist` |
| Bug investigation | `systematic-debugger` |
| Code quality improvement | `architecture-refactoring` |

### Workflow Integration

All agents follow the IReader project's established workflows:

1. **TDD Methodology** - All agents write tests first (RED-GREEN-REFACTOR)
2. **Superpowers Workflow** - Systematic approach to development
3. **Clean Architecture** - Respect layer boundaries
4. **Frequent Commits** - After each passing test

### Reference Documents

Agents reference these steering files:
- `.kiro/steering/tdd-methodology.md` - TDD workflow
- `.kiro/steering/superpowers-overview.md` - Systematic development
- `.kiro/steering/superpowers-systematic-debugging.md` - Debugging process
- `.kiro/steering/superpowers-brainstorming.md` - Design process
- `.kiro/steering/superpowers-writing-plans.md` - Implementation planning

## Testing Agents

You can test an agent by invoking it with a simple task:

```
@kmp-feature-developer Write a simple use case that returns a greeting message
```

The agent should:
1. Write a failing test first
2. Implement minimal code to pass
3. Refactor if needed
4. Run tests and verify

## Agent Capabilities

### All Agents Have Access To:
- **read** tools - File reading, searching, diagnostics
- **write** tools - File creation, modification, deletion
- **shell** tools - Running gradle commands, tests

### Extension Source Builder Also Has:
- **web** tools - For fetching and analyzing website HTML

## Best Practices

1. **Be specific** - Give agents clear, focused tasks
2. **One agent at a time** - Don't mix concerns
3. **Trust the process** - Agents follow TDD strictly
4. **Review tests** - Agents write tests first, verify they make sense
5. **Commit frequently** - Agents commit after each passing test

## Troubleshooting

### Agent not following TDD?
- Check that tests are written first
- Verify tests fail before implementation
- Ensure tests pass after implementation

### Agent breaking architecture?
- Review layer dependencies
- Check that domain doesn't depend on data/presentation
- Verify interfaces are in the correct layer

### Agent making too many changes?
- Ask for smaller, incremental steps
- Request one feature at a time
- Break complex tasks into subtasks

## Customization

These agents are workspace-specific and can be modified:
- Edit `.kiro/agents/<agent-name>.md` to customize behavior
- Modify system prompts to add project-specific guidelines
- Adjust tool access if needed

## Feedback

If an agent isn't working as expected:
1. Review the agent's system prompt
2. Check if the task matches the agent's specialization
3. Provide more specific instructions
4. Consider using a different agent for the task
