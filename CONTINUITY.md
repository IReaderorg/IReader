# Continuity Ledger

## Goal (incl. success criteria)
- Redesign quote feature with Instagram story-style UI and Discord webhook integration
- Success: Users can create quotes with visual styles, save locally or share to Discord

## Constraints/Assumptions
- Kotlin Multiplatform project (Android/iOS/Desktop)
- Discord webhook URL provided: https://discord.com/api/webhooks/1476522836611960885/XuZRN0qG80ectOolOG03EQ-OFPkPp0Vi_GF5WDflnD-0mGvFnRehbjaW3RdFQq1b4OjWi
- Keep local quote storage (unlimited length)
- Remove Supabase quote repository
- Follow implementation plan: 31 tasks across 5 phases

## Key Decisions
- Instagram story-style quote creation (swipe styles, tap to edit)
- Discord webhook submission (like character art)
- Keep local quote storage and "My Quotes" tab
- Remove community quotes viewing screens
- Dual save options: "Save Locally" or "Share to Discord"

## State

### Done
- ✅ Design document written and committed
- ✅ Implementation plan created (31 tasks, 5 phases)
- ✅ Task 1.1: Added DISCORD_QUOTE_WEBHOOK_URL to BuildConfig (all platforms)

### Now
- Phase 1: Setup & Infrastructure
- Task 1.2: Create Discord Quote Repository Interface

### Next
- Task 1.3: Implement Discord Quote Repository
- Task 1.4: Update DI Module
- Continue through Phase 1 tasks

## Open Questions
- None

## Working Set (files/ids/commands)
- domain/build.gradle.kts (BuildConfig)
- domain/src/commonMain/kotlin/ireader/domain/config/PlatformConfig.kt
- domain/src/androidMain/kotlin/ireader/domain/config/PlatformConfig.kt
- domain/src/desktopMain/kotlin/ireader/domain/config/PlatformConfig.kt
- domain/src/iosMain/kotlin/ireader/domain/config/PlatformConfig.ios.kt
- docs/plans/2025-02-26-quote-feature-redesign.md
- docs/plans/2025-02-26-quote-feature-implementation-plan.md
