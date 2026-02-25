# Continuity Ledger (Compaction-Safe)

Maintain a single Continuity Ledger for this workspace in `CONTINUITY.md`. The ledger is the canonical session briefing designed to survive context compaction; do not rely on earlier chat text unless it's reflected in the ledger.

## How It Works

- At the start of every assistant turn: read `CONTINUITY.md`, update it to reflect the latest goal/constraints/decisions/state, then proceed with the work.
- Update `CONTINUITY.md` again whenever any of these change: goal, constraints/assumptions, key decisions, progress state (Done/Now/Next), or important tool outcomes.
- Keep it short and stable: facts only, no transcripts. Prefer bullets. Mark uncertainty as UNCONFIRMED (never guess).
- If you notice missing recall or a compaction/summary event: refresh/rebuild the ledger from visible context, mark gaps UNCONFIRMED, ask up to 1–3 targeted questions, then continue.

## Ledger vs Task Plans

- Task plans (if used) are for short-term execution scaffolding while you work (a small 3–7 step plan with pending/in_progress/completed).
- `CONTINUITY.md` is for long-running continuity across compaction (the "what/why/current state"), not a step-by-step task list.
- Keep them consistent: when the plan or state changes, update the ledger at the intent/progress level (not every micro-step).

## In Replies

- Begin with a brief "Ledger Snapshot" (Goal + Now/Next + Open Questions). Print the full ledger only when it materially changes or when the user asks.

## `CONTINUITY.md` Format

Keep these headings in the ledger file:

```markdown
# Continuity Ledger

## Goal (incl. success criteria)
- [Primary objective and how we'll know it's complete]

## Constraints/Assumptions
- [Technical constraints, platform requirements, dependencies]
- [Assumptions about the codebase or user environment]

## Key Decisions
- [Important architectural or implementation choices made]
- [Rationale for non-obvious decisions]

## State

### Done
- [Completed work items]

### Now
- [Current focus/active work]

### Next
- [Immediate next steps]

## Open Questions (UNCONFIRMED if needed)
- [Unresolved questions or uncertainties]
- [Items marked UNCONFIRMED when context is unclear]

## Working Set (files/ids/commands)
- [Key files being modified]
- [Important identifiers, commands, or references]
```

## Best Practices

1. **Update at Turn Start**: Always read and refresh the ledger before beginning work
2. **Update on Change**: Whenever goals, decisions, or state changes, update the ledger
3. **Be Concise**: Use bullets, avoid prose, stick to facts
4. **Mark Uncertainty**: Use UNCONFIRMED for anything unclear after compaction
5. **No Duplication**: Don't repeat information that's already in the codebase or other steering docs
6. **Survive Compaction**: Write as if the chat history will disappear - the ledger should be self-contained

## Example Ledger Entry

```markdown
# Continuity Ledger

## Goal (incl. success criteria)
- Implement TTS (Text-to-Speech) pause/resume functionality in ReaderTTSViewModel
- Success: User can pause and resume TTS playback without losing position

## Constraints/Assumptions
- Kotlin Multiplatform project (Android/iOS)
- Using platform-specific TTS engines
- Must follow TDD methodology (RED-GREEN-REFACTOR)

## Key Decisions
- Use StateFlow for pause/resume state management
- Platform-specific implementations in expect/actual pattern
- Store current utterance position for resume functionality

## State

### Done
- Created failing test for pause functionality
- Implemented basic pause method

### Now
- Implementing resume functionality with position tracking

### Next
- Add tests for edge cases (pause when not playing, resume when already playing)
- Refactor for cleaner state management

## Open Questions
- UNCONFIRMED: Does iOS TTS API support mid-utterance pause/resume?

## Working Set (files/ids/commands)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderTTSViewModel.kt
- presentation/src/androidMain/kotlin/ireader/presentation/ui/reader/tts/AndroidTTSEngine.kt
- presentation/src/iosMain/kotlin/ireader/presentation/ui/reader/tts/IosTTSEngine.kt
```

## When to Create/Update the Ledger

Create `CONTINUITY.md` when:
- Starting a new feature or significant work session
- User explicitly requests continuity tracking
- Working on complex multi-session tasks

Update the ledger when:
- Goals or success criteria change
- Important decisions are made
- Progress state changes (Done/Now/Next)
- New constraints or assumptions are discovered
- Questions are answered or new ones arise
- Key files in the working set change

## Integration with TDD

When following TDD methodology:
- Update "Now" when entering RED/GREEN/REFACTOR phases
- Record key decisions about test structure or implementation approach
- Track which tests are written vs passing in the State section
- Note any TDD violations or deviations in Open Questions
