# Implementation Plans

This directory contains design documents and implementation plans following the Superpowers methodology.

## File Naming Convention

- Design documents: `YYYY-MM-DD-<feature-name>-design.md`
- Implementation plans: `YYYY-MM-DD-<feature-name>-plan.md`

## Workflow

### 1. Design Phase (Brainstorming)

Before writing any code, create a design document:

```markdown
# Feature Name - Design

**Date:** YYYY-MM-DD
**Status:** Draft | Approved | Implemented

## Goal

One sentence describing what this builds.

## Context

Current state, problem being solved, constraints.

## Proposed Approaches

### Option 1: [Recommended]

Description, pros, cons.

### Option 2:

Description, pros, cons.

### Option 3:

Description, pros, cons.

## Selected Approach

Which option and why.

## Architecture

Components, data flow, interactions.

## Testing Strategy

How to verify it works.

## Risks & Mitigations

What could go wrong and how to handle it.
```

### 2. Implementation Phase (Writing Plans)

After design approval, create implementation plan:

```markdown
# Feature Name - Implementation Plan

**Goal:** One sentence

**Architecture:** 2-3 sentences

**Tech Stack:** Key technologies

---

### Task 1: Component Name

**Files:**
- Create: `exact/path/to/file.kt`
- Modify: `exact/path/to/existing.kt:123-145`
- Test: `tests/exact/path/to/test.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `test behavior`() {
    // test code
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :module:test`
Expected: FAIL

**Step 3: Write minimal implementation**

```kotlin
// implementation code
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :module:test`
Expected: PASS

**Step 5: Commit**

```bash
git add ...
git commit -m "feat: ..."
```
```

## Examples

See existing plans in this directory for examples of:
- Feature designs
- Bug fix plans
- Refactoring plans
- Extension source plans

## Related Documentation

- `../.kiro/steering/superpowers-overview.md` - Overall workflow
- `../.kiro/steering/superpowers-brainstorming.md` - Design process
- `../.kiro/steering/superpowers-writing-plans.md` - Implementation planning
- `../.kiro/steering/tdd-methodology.md` - Test-driven development
- `../.kiro/steering/superpowers-systematic-debugging.md` - Debugging process
