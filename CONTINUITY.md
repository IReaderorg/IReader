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
- Chapter art prompt generation now uses translation engine architecture:
  * Added `generateContent()` method to TranslateEngine base class
  * TranslationEnginesManager provides unified interface for content generation
  * ChapterArtPromptGenerator delegates to TranslationEnginesManager
  * Cleaner separation of concerns - no HTTP/API logic in ChapterArtPromptGenerator

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
- ✅ Updated Pollinations to require API key (free tier available)
- ✅ Removed HuggingFace and Stability AI providers
- ✅ Tested Qwen (HuggingFace) - takes 3 minutes to generate (too slow)
- ✅ Removed slow Qwen provider from codebase
- ✅ Fixed UnifiedImageGenerator.kt (removed HUGGING_FACE/STABILITY_AI/old QWEN)
- ✅ Fixed CharacterArtUploadScreenSpec.kt (removed old provider references)
- ✅ Fixed UploadCharacterArtScreen.kt (updated function parameters)
- ✅ Fixed MultiProviderApiKeyDialog (updated API key placeholders)
- ✅ Added pollinationsApiKey() preference to ReaderPreferences.kt
- ✅ Reordered AIProviderOption enum - Pollinations first
- ✅ Added isVisible flag to hide Gemini from UI (logic kept)
- ✅ Updated ProviderSelectorDialog to filter visible providers
- ✅ Added "Get Free API Key" button in MultiProviderApiKeyDialog
- ✅ Added URI handler to open https://enter.pollinations.ai/
- ✅ Fixed Discord "View Channel" button with uriHandler
- ✅ Added DISCORD_CHARACTER_ART_WEBHOOK_URL to GitHub Actions (Release.yaml and Preview.yaml)
- ✅ Created QwenFastImageGenerator.kt (new fast Qwen using multimodalart-qwen-image-fast.hf.space)
- ✅ Added QWEN_FAST to ImageProvider enum (FREE, no API key, ~15 seconds)
- ✅ Updated UnifiedImageGenerator to support QWEN_FAST
- ✅ Added QWEN_FAST to AIProviderOption enum (visible, first option)
- ✅ Changed default provider to QWEN_FAST
- ✅ Final providers: QWEN_FAST (free, default), POLLINATIONS (paid), GEMINI (hidden)
- ✅ Made brush icon (chapter art prompt generator) use default translation engine instead of hardcoded Gemini
- ✅ Updated ChapterArtPromptGenerator to accept TranslationEnginesManager
- ✅ Updated ReaderScreenViewModel to pass translationEnginesManager to ChapterArtPromptGenerator
- ✅ Changed error message from "Please set your Gemini API key" to "Please configure your translation engine API key"
- ✅ Refactored architecture: Added `generateContent()` method to TranslateEngine base class
- ✅ Added `generateContent()` implementation to GeminiTranslateEngine
- ✅ Added `generateContent()` wrapper method to TranslationEnginesManager
- ✅ Simplified ChapterArtPromptGenerator to call TranslationEnginesManager.generateContent()
- ✅ Removed HttpClient and Gemini-specific code from ChapterArtPromptGenerator
- ✅ Updated ReaderScreenViewModel to pass only TranslationEnginesManager (no HttpClient)
- ✅ Added `generateContent()` implementation to all built-in AI engines:
  * OpenAITranslateEngine - uses GPT-3.5-turbo
  * DeepSeekTranslateEngine - uses deepseek-chat
  * OpenRouterTranslateEngine - uses user-selected model or auto
  * NvidiaTranslateEngine - uses user-selected model or llama-3.1-8b-instruct
  * GeminiTranslateEngine - uses user-selected model or gemini-2.0-flash
- ✅ Removed redundant API Key fields from OpenRouter and NVIDIA configuration sections
- ✅ Moved OpenRouter and NVIDIA API Key input to main API Key section (unified with other engines)
- ✅ OpenRouter and NVIDIA now only show model selection and "Fetch Models" button
- ✅ Fixed Qwen Fast timeout issue:
  * Increased polling timeout from 30 seconds to 60 seconds (30 attempts × 2 seconds)
  * Added explicit HTTP timeout configuration to all requests (60s request, 30s connect, 60s socket)
  * Applied timeouts to initiateGeneration(), fetchResult(), and downloadImage() methods
- ✅ Fixed WebscrapingTranslateEngine client visibility:
  * Changed `private val client` to `protected val client` so GeminiTranslateEngine can access it
  * Allows child classes to use client.default for HTTP requests
- ✅ Fixed QwenFastImageGenerator timeout import (removed wildcard from import)
- ✅ Fixed ReaderChapterArtViewModel - removed apiKey parameter from generateImagePrompt call
- ✅ Fixed ReaderScreenViewModel - removed obsolete chapterArtHttpClient cleanup code
- ✅ Enhanced Qwen Fast polling with better debugging:
  * Increased timeout to 90 seconds (30 attempts × 3 seconds)
  * Added detailed logging for debugging (attempt number, response length, event data keys, image URL)
  * Support multiple response formats (nested array or direct object)
  * Better error handling and reporting
- ✅ Updated Community Hub navigation:
  * Changed "Character Art Gallery" to "Upload Character Art" (navigates to upload screen)
  * Added "Character Art Discord" button to open Discord channel
  * Qwen Fast is already first in AIProviderOption enum (confirmed)
- ✅ Set Qwen Fast as default provider:
  * Changed CharacterArtViewModel default from POLLINATIONS to QWEN_FAST
  * UploadCharacterArtScreen already defaults to QWEN_FAST
  * Both ImageProvider and AIProviderOption enums have QWEN_FAST first

### Now
- All changes complete

### Next
- User should test Qwen Fast image generation with enhanced logging
- User needs to replace Discord URL in CommunityHubScreen.kt (line with "https://discord.gg/your-channel-here")
- User should test brush icon with different translation engines
- User needs to add DISCORD_CHARACTER_ART_WEBHOOK_URL secret to GitHub repository settings

## Open Questions
- None

## Working Set (files/ids/commands)
- domain/src/commonMain/kotlin/ireader/domain/data/engines/TranslateEngine.kt (added generateContent method)
- domain/src/commonMain/kotlin/ireader/domain/usecases/translate/TranslationEnginesManager.kt (added generateContent wrapper)
- domain/src/commonMain/kotlin/ireader/domain/usecases/translate/WebscrapingTranslateEngine.kt (Gemini generateContent)
- domain/src/commonMain/kotlin/ireader/domain/usecases/translate/OpenAITranslateEngine.kt (added generateContent)
- domain/src/commonMain/kotlin/ireader/domain/usecases/translate/DeepSeekTranslateEngine.kt (added generateContent)
- domain/src/commonMain/kotlin/ireader/domain/usecases/translate/OpenRouterTranslateEngine.kt (added generateContent)
- domain/src/commonMain/kotlin/ireader/domain/usecases/translate/NvidiaTranslateEngine.kt (added generateContent)
- data/src/commonMain/kotlin/ireader/data/characterart/ChapterArtPromptGenerator.kt (simplified)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt (updated)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/translation/TranslationSettingsScreenV2.kt (added OpenRouter/NVIDIA to API key section)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/translation/EngineSpecificConfig.kt (removed redundant API key fields)
