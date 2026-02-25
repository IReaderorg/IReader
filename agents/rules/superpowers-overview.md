---
inclusion: auto
description: Overview of Superpowers systematic software development workflow - brainstorm, plan, TDD, debug
---

# Superpowers: Systematic Software Development

## Overview

Superpowers is a complete software development workflow built on composable skills and systematic processes. It transforms AI agents from helpful assistants into disciplined software engineers.

**Core Philosophy:**
- Test-Driven Development - Write tests first, always
- Systematic over ad-hoc - Process over guessing
- Complexity reduction - Simplicity as primary goal
- Evidence over claims - Verify before declaring success

## The Basic Workflow

### 1. Brainstorming (Before Code)

**Activates:** Before writing any code

**Process:**
- Refines rough ideas through questions
- Explores alternatives
- Presents design in sections for validation
- Saves design document

**Output:** Approved design document in `docs/plans/YYYY-MM-DD-<topic>-design.md`

**See:** `superpowers-brainstorming.md`

### 2. Writing Plans (After Design)

**Activates:** With approved design

**Process:**
- Breaks work into bite-sized tasks (2-5 minutes each)
- Every task has exact file paths
- Every task has complete code
- Every task has verification steps

**Output:** Implementation plan in `docs/plans/YYYY-MM-DD-<feature>-plan.md`

**See:** `superpowers-writing-plans.md`

### 3. Test-Driven Development (During Implementation)

**Activates:** During all implementation

**Process:**
- RED: Write failing test
- Verify it fails correctly
- GREEN: Write minimal code
- Verify it passes
- REFACTOR: Clean up
- Commit

**Rule:** NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST

**See:** `tdd-methodology.md`

### 4. Systematic Debugging (When Issues Arise)

**Activates:** For any bug, test failure, or unexpected behavior

**Process:**
- Phase 1: Root cause investigation (gather evidence)
- Phase 2: Pattern analysis (find working examples)
- Phase 3: Hypothesis and testing (scientific method)
- Phase 4: Implementation (fix root cause, not symptom)

**Rule:** NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST

**See:** `superpowers-systematic-debugging.md`

## Mandatory Workflows

These are not suggestions. The agent checks for relevant skills before any task.

### Before Writing Code

1. **Brainstorm** - Understand requirements, explore alternatives, get design approval
2. **Write Plan** - Break into bite-sized tasks with exact files and code

### During Implementation

1. **TDD** - Every feature, every bug fix, every change
2. **Frequent Commits** - After each passing test

### When Debugging

1. **Systematic Debugging** - Find root cause before fixing
2. **Create Test** - Reproduce bug in test before fixing

## IReader-Specific Workflow

### New Feature

```
1. Brainstorm
   ↓
2. Write Design Doc (docs/plans/YYYY-MM-DD-feature-design.md)
   ↓
3. Get User Approval
   ↓
4. Write Implementation Plan (docs/plans/YYYY-MM-DD-feature-plan.md)
   ↓
5. For Each Task:
   - Write failing test
   - Verify it fails
   - Write minimal code
   - Verify it passes
   - Commit
   ↓
6. Verify all tests pass
   ↓
7. Create PR
```

### Bug Fix

```
1. Systematic Debugging
   ↓
2. Write failing test that reproduces bug
   ↓
3. Verify test fails
   ↓
4. Fix root cause (not symptom)
   ↓
5. Verify test passes
   ↓
6. Verify no regressions
   ↓
7. Commit
```

### Extension Source

```
1. Brainstorm
   - Identify site type (Madara, SourceFactory, API)
   - Check HTML structure
   - Explore alternatives
   ↓
2. Write Plan
   - Use KSP annotations where possible
   - Exact selectors
   - Test cases
   ↓
3. Implement with TDD
   - Test selector parsing
   - Test novel list
   - Test chapter list
   - Test content
   ↓
4. Build and test
   - ./gradlew :sources:lang:name:assembleLangDebug
   - ./gradlew testServer
```

## Key Principles

### YAGNI (You Aren't Gonna Need It)

Don't add features until they're needed. Remove unnecessary complexity from all designs.

**Examples:**
- ❌ "Let's add configuration for future flexibility"
- ✅ "Hard-code it now, extract when second use case appears"

### DRY (Don't Repeat Yourself)

Extract duplication, but only after it exists. Don't predict future duplication.

**Examples:**
- ❌ "Let's create abstraction for potential future cases"
- ✅ "This code appears twice, let's extract it"

### TDD Always

No exceptions. If you think "skip TDD just this once", that's rationalization.

**Examples:**
- ❌ "This is too simple to test"
- ✅ "Simple code breaks. Test takes 30 seconds."

### Systematic Over Ad-Hoc

Follow the process, especially when you think you don't need to.

**Examples:**
- ❌ "I see the problem, let me fix it"
- ✅ "Let me investigate root cause first"

## Red Flags

If you catch yourself thinking:

### Before Code
- "This is too simple to need a design"
- "Let me just start coding and see"
- "I'll document it after"

### During Implementation
- "I'll write tests after"
- "This is too simple to test"
- "Let me just try this quickly"

### During Debugging
- "Quick fix for now, investigate later"
- "Just try changing X and see"
- "I don't fully understand but this might work"

**All of these mean: STOP. Follow the process.**

## Benefits

### Design First
- Catches issues before coding
- Explores alternatives
- Gets user buy-in
- Creates documentation

### TDD
- Finds bugs before commit
- Prevents regressions
- Documents behavior
- Enables refactoring

### Systematic Debugging
- Faster than guess-and-check
- Fixes root cause, not symptom
- Prevents similar bugs
- Builds understanding

## Integration with IReader

### Steering Files

All Superpowers skills are available as steering files:
- `superpowers-overview.md` - This file
- `superpowers-brainstorming.md` - Design before code
- `superpowers-writing-plans.md` - Implementation plans
- `superpowers-systematic-debugging.md` - Root cause debugging
- `tdd-methodology.md` - Test-driven development

### Automatic Activation

These skills activate automatically based on context:
- Brainstorming: When user asks to build something
- TDD: During any implementation
- Systematic Debugging: When encountering issues
- Writing Plans: After design approval

### Manual Invocation

You can also explicitly reference these skills:
- "Let's brainstorm this feature first"
- "Follow TDD for this implementation"
- "Use systematic debugging to find root cause"
- "Write an implementation plan"

## Remember

> "Code without tests is broken by design." - Jacob Kaplan-Moss

> "If you didn't watch the test fail, you don't know if it tests the right thing." - Superpowers TDD

> "Symptom fixes are failure." - Superpowers Debugging

Always follow the process. The time invested pays off exponentially in maintenance and debugging time saved.
