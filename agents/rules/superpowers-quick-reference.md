---
inclusion: manual
---

# Superpowers Quick Reference

## When to Use Each Skill

| Situation | Skill | Action |
|-----------|-------|--------|
| User asks to build something | Brainstorming | Ask questions, explore alternatives, present design |
| Design approved | Writing Plans | Break into bite-sized tasks with exact files/code |
| Implementing any feature | TDD | RED-GREEN-REFACTOR cycle |
| Bug or test failure | Systematic Debugging | Find root cause before fixing |
| "Quick fix" temptation | Systematic Debugging | STOP. Investigate first. |
| Test passes immediately | TDD | Delete code. Start over. |
| 3+ fixes failed | Systematic Debugging | Question architecture |

## The Iron Laws

### Brainstorming
```
NO IMPLEMENTATION WITHOUT DESIGN APPROVAL
```

### TDD
```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```

### Systematic Debugging
```
NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST
```

## Red Flags

### Stop and Brainstorm If:
- "This is too simple to need a design"
- "Let me just start coding"
- "I'll document it after"

### Stop and Follow TDD If:
- "I'll write tests after"
- "This is too simple to test"
- "Let me just try this quickly"
- Test passes immediately
- "Keep as reference, write tests first"

### Stop and Debug Systematically If:
- "Quick fix for now"
- "Just try changing X"
- "I don't fully understand but..."
- Already tried 2+ fixes
- Each fix reveals new problem

## Quick Workflows

### New Feature
```
1. Brainstorm → Design Doc
2. Get Approval
3. Write Plan → Implementation Plan
4. For Each Task: RED-GREEN-REFACTOR-COMMIT
5. Verify All Tests Pass
```

### Bug Fix
```
1. Systematic Debugging → Root Cause
2. Write Failing Test
3. Verify Test Fails
4. Fix Root Cause
5. Verify Test Passes
6. Commit
```

### Extension Source
```
1. Brainstorm → Identify Site Type
2. Write Plan → Selectors + Tests
3. Implement with TDD
4. Build and Test
```

## TDD Cycle

```
RED: Write failing test
  ↓
Verify it fails correctly
  ↓
GREEN: Write minimal code
  ↓
Verify it passes
  ↓
REFACTOR: Clean up
  ↓
Verify still passes
  ↓
COMMIT
  ↓
Next test
```

## Debugging Phases

```
Phase 1: ROOT CAUSE
- Read errors carefully
- Reproduce consistently
- Check recent changes
- Gather evidence
- Trace data flow

Phase 2: PATTERN ANALYSIS
- Find working examples
- Compare against references
- Identify differences
- Understand dependencies

Phase 3: HYPOTHESIS
- Form single hypothesis
- Test minimally
- Verify before continuing

Phase 4: IMPLEMENTATION
- Create failing test
- Implement single fix
- Verify fix works
- If 3+ fixes failed → Question architecture
```

## Common Rationalizations

| Excuse | Reality |
|--------|---------|
| "Too simple to need X" | Simple things break too |
| "I'll do X after" | After = never or wrong |
| "No time for process" | Process is faster than thrashing |
| "Just this once" | That's rationalization |
| "I already tested manually" | Manual ≠ systematic |
| "Deleting X hours is wasteful" | Sunk cost fallacy |
| "TDD is dogmatic" | TDD IS pragmatic |

## File Locations

- Design docs: `docs/plans/YYYY-MM-DD-<feature>-design.md`
- Implementation plans: `docs/plans/YYYY-MM-DD-<feature>-plan.md`
- Steering files: `.kiro/steering/superpowers-*.md`

## Key Principles

- **YAGNI** - You Aren't Gonna Need It
- **DRY** - Don't Repeat Yourself (after duplication exists)
- **TDD Always** - No exceptions
- **Systematic Over Ad-Hoc** - Process over guessing
- **Evidence Over Claims** - Verify before declaring success

## Remember

> "If you didn't watch the test fail, you don't know if it tests the right thing."

> "Symptom fixes are failure."

> "Code without tests is broken by design."

Follow the process, especially when you think you don't need to.
