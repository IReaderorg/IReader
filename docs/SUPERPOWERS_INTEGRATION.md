# Superpowers Integration

This document explains the Superpowers methodology integration into IReader.

## What is Superpowers?

Superpowers is a systematic software development workflow created by Jesse Vincent (obra). It transforms AI coding agents from helpful assistants into disciplined software engineers through composable skills and mandatory processes.

**Source:** https://github.com/obra/superpowers

## Core Philosophy

1. **Test-Driven Development** - Write tests first, always
2. **Systematic over ad-hoc** - Process over guessing
3. **Complexity reduction** - Simplicity as primary goal
4. **Evidence over claims** - Verify before declaring success

## Integrated Skills

### 1. Brainstorming (Design Before Code)

**File:** `.kiro/steering/superpowers-brainstorming.md`

**Purpose:** Turn ideas into fully formed designs through collaborative dialogue.

**Process:**
- Explore project context
- Ask clarifying questions (one at a time)
- Propose 2-3 approaches with trade-offs
- Present design in sections for approval
- Save design document

**Hard Gate:** NO implementation without design approval.

### 2. Writing Plans (Implementation Planning)

**File:** `.kiro/steering/superpowers-writing-plans.md`

**Purpose:** Create comprehensive implementation plans with bite-sized tasks.

**Process:**
- Break work into 2-5 minute tasks
- Provide exact file paths
- Include complete code (not pseudocode)
- Specify verification steps
- Follow DRY, YAGNI, TDD

**Output:** Implementation plan in `docs/plans/YYYY-MM-DD-<feature>-plan.md`

### 3. Test-Driven Development (Implementation)

**File:** `.kiro/steering/tdd-methodology.md` (enhanced)

**Purpose:** Ensure all code is tested and works correctly.

**Process:**
- RED: Write failing test
- Verify it fails correctly
- GREEN: Write minimal code
- Verify it passes
- REFACTOR: Clean up
- Commit

**Iron Law:** NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST

### 4. Systematic Debugging (Problem Solving)

**File:** `.kiro/steering/superpowers-systematic-debugging.md`

**Purpose:** Find root causes before attempting fixes.

**Process:**
- Phase 1: Root cause investigation
- Phase 2: Pattern analysis
- Phase 3: Hypothesis and testing
- Phase 4: Implementation

**Iron Law:** NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST

## File Structure

```
IReader/
├── .kiro/
│   └── steering/
│       ├── superpowers-overview.md           # Main overview
│       ├── superpowers-brainstorming.md      # Design process
│       ├── superpowers-writing-plans.md      # Implementation planning
│       ├── superpowers-systematic-debugging.md # Debugging process
│       ├── superpowers-quick-reference.md    # Quick reference (manual)
│       ├── tdd-methodology.md                # Enhanced TDD (existing)
│       ├── ireader-extensions-ksp.md         # Updated with references
│       └── AI_SOURCE_GENERATOR_PROMPT.md     # Updated with references
└── docs/
    └── plans/
        ├── README.md                         # Plans directory guide
        ├── YYYY-MM-DD-<feature>-design.md   # Design documents
        └── YYYY-MM-DD-<feature>-plan.md     # Implementation plans
```

## Workflow Examples

### New Feature

```
1. User: "Add paragraph indent setting"
   ↓
2. Brainstorm:
   - Ask questions about requirements
   - Explore UI placement options
   - Present design for approval
   - Save to docs/plans/2026-02-13-paragraph-indent-design.md
   ↓
3. Write Plan:
   - Break into tasks (add preference, add UI, add logic)
   - Each task has exact files and code
   - Save to docs/plans/2026-02-13-paragraph-indent-plan.md
   ↓
4. Implement with TDD:
   Task 1: Add preference
     - Write failing test
     - Verify fails
     - Write minimal code
     - Verify passes
     - Commit
   Task 2: Add UI
     - Write failing test
     - ...
   ↓
5. Verify all tests pass
   ↓
6. Create PR
```

### Bug Fix

```
1. User: "Chapter content not displaying"
   ↓
2. Systematic Debugging:
   Phase 1: Root Cause
     - Read error messages
     - Reproduce consistently
     - Check recent changes
     - Trace data flow
   Phase 2: Pattern Analysis
     - Find working examples
     - Identify differences
   Phase 3: Hypothesis
     - "Content selector changed"
     - Test hypothesis
   Phase 4: Implementation
     - Write failing test
     - Fix selector
     - Verify test passes
     - Commit
```

### Extension Source

```
1. User: "Add source for NovelSite.com"
   ↓
2. Brainstorm:
   - Check if Madara site
   - Inspect HTML structure
   - Identify selectors
   - Present design
   ↓
3. Write Plan:
   - Use @MadaraSource or SourceFactory
   - List exact selectors
   - Test cases for each component
   ↓
4. Implement with TDD:
   - Test novel list parsing
   - Test chapter list parsing
   - Test content parsing
   ↓
5. Build and test:
   - ./gradlew :sources:en:novelsite:assembleEnDebug
   - ./gradlew testServer
```

## Benefits

### Design First
- Catches issues before coding
- Explores alternatives
- Gets user buy-in
- Creates documentation

### TDD
- Finds bugs before commit (faster than debugging after)
- Prevents regressions (tests catch breaks immediately)
- Documents behavior (tests show how to use code)
- Enables refactoring (change freely, tests catch breaks)

### Systematic Debugging
- Faster than guess-and-check thrashing
- Fixes root cause, not symptom
- Prevents similar bugs
- Builds understanding

### Implementation Plans
- Clear roadmap
- Bite-sized tasks
- Exact specifications
- Easy to review

## Integration with Existing Workflow

### Steering Files

All Superpowers skills are integrated as steering files with `inclusion: auto`, meaning they're automatically available to Kiro.

### Existing Files Enhanced

- `tdd-methodology.md` - Enhanced with stricter TDD rules and common rationalizations
- `ireader-extensions-ksp.md` - Added references to Superpowers skills
- `AI_SOURCE_GENERATOR_PROMPT.md` - Added references to Superpowers skills

### New Directories

- `docs/plans/` - Design documents and implementation plans

## Usage

### Automatic Activation

Kiro will automatically use these skills based on context:
- Brainstorming: When user asks to build something
- TDD: During any implementation
- Systematic Debugging: When encountering issues
- Writing Plans: After design approval

### Manual Reference

You can explicitly reference skills:
- "Let's brainstorm this feature first"
- "Follow TDD for this implementation"
- "Use systematic debugging to find root cause"
- "Write an implementation plan"

### Quick Reference

For a quick overview, see `.kiro/steering/superpowers-quick-reference.md` (manual inclusion).

## Key Principles

### YAGNI (You Aren't Gonna Need It)

Don't add features until they're needed. Remove unnecessary complexity from all designs.

### DRY (Don't Repeat Yourself)

Extract duplication, but only after it exists. Don't predict future duplication.

### TDD Always

No exceptions. If you think "skip TDD just this once", that's rationalization.

### Systematic Over Ad-Hoc

Follow the process, especially when you think you don't need to.

## Red Flags

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

## The Iron Laws

```
NO IMPLEMENTATION WITHOUT DESIGN APPROVAL
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST
```

## Credits

Superpowers framework created by Jesse Vincent (obra).

**Repository:** https://github.com/obra/superpowers

**License:** MIT

## Further Reading

- Superpowers README: https://github.com/obra/superpowers/blob/main/README.md
- TDD Skill: https://github.com/obra/superpowers/blob/main/skills/test-driven-development/SKILL.md
- Brainstorming Skill: https://github.com/obra/superpowers/blob/main/skills/brainstorming/SKILL.md
- Systematic Debugging: https://github.com/obra/superpowers/blob/main/skills/systematic-debugging/SKILL.md
- Writing Plans: https://github.com/obra/superpowers/blob/main/skills/writing-plans/SKILL.md
