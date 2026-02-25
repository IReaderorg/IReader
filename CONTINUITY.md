# Continuity Ledger

## Goal (incl. success criteria)
- Remove R2+Supabase character art integration completely
- Remove character art verification UI and logic
- Redesign character art gallery to be Discord-focused
- Success: Clean codebase with only Discord integration, no verification workflow, simplified gallery UI

## Constraints/Assumptions
- Kotlin Multiplatform project (Android/iOS/Desktop)
- Discord webhook is now the only backend (no R2+Supabase fallback)
- Gallery shows "recently posted" and link to Discord channel
- Keep image generation UI (Gemini, HuggingFace, Pollinations, etc.)
- No approval/verification workflow needed (Discord handles moderation)

## Key Decisions
- Removed all R2 storage code - DONE
- Removed all Supabase character art metadata code - DONE
- Removed CharacterArtRepositoryImpl (use only DiscordCharacterArtRepository) - DONE
- Simplified DI module to Discord only - DONE
- Redesigned gallery UI (Discord-focused) - DONE
- Added missing i18n strings - DONE
- Removed R2 config from PlatformConfig and BuildConfig - DONE
- Removed CharacterArtStatus enum (no approval workflow) - DONE
- Removed admin verification screen and navigation - DONE
- Removed AutoApproveCharacterArtUseCase - DONE

## State

### Done
- ✅ Fixed Pollinations.ai (new unified API)
- ✅ Created Discord webhook integration
- ✅ Deleted R2+Supabase repository files
- ✅ Simplified DI module (Discord only)
- ✅ Redesigned CharacterArtGalleryScreen
- ✅ Added missing i18n strings
- ✅ Removed R2 config from all PlatformConfig files
- ✅ Removed R2 BuildConfig fields from domain/build.gradle.kts
- ✅ Removed CharacterArtStatus enum from CharacterArt model
- ✅ Removed admin verification screen (AdminCharacterArtVerificationScreenSpec.kt)
- ✅ Removed verification navigation route
- ✅ Removed verification UI from CommunityHubScreen
- ✅ Fixed DiscordCharacterArtRepository (removed status field references)
- ✅ Fixed PollinationsImageGenerator (added missing import, removed extra brace)

### Now
- Testing compilation

### Next
- Fix any remaining compilation errors
- Test the complete flow: generate art → post to Discord → view in gallery
- Update CharacterArtDetailScreen if needed

## Open Questions
- None

## Working Set (files/ids/commands)
- domain/src/commonMain/kotlin/ireader/domain/models/characterart/CharacterArt.kt
- data/src/commonMain/kotlin/ireader/data/repository/DiscordCharacterArtRepository.kt
- data/src/commonMain/kotlin/ireader/data/characterart/PollinationsImageGenerator.kt
- presentation/src/commonMain/kotlin/ireader/presentation/core/NavigationRoutes.kt
- presentation/src/commonMain/kotlin/ireader/presentation/core/CommonNavHost.kt
- presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/CommunityHubScreen.kt
