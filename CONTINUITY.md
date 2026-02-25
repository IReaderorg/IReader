# Continuity Ledger

## Goal (incl. success criteria)
- Implement text replacement feature for IReader (find-and-replace functionality)
- Success: Users can create, edit, and delete text replacements that automatically apply when reading chapters or using TTS

## Constraints/Assumptions
- Kotlin Multiplatform project (Android/iOS/Desktop)
- User explicitly requested NOT to follow TDD methodology
- Text replacements should be applied BEFORE content filtering in the processing pipeline
- SQLDelight for database with custom migration system

## Key Decisions
- Text replacement applied before content filtering (user requirement)
- Replaced content filter UI in Reader settings and TTS settings with text replacement links
- Used SQLDelight migration 38 for text_replacement table
- Navigation route: NavigationRoutes.textReplacement

## State

### Done
- All previous implementation complete
- Improved TextReplacementScreen UI with toolbar and better layout
- Added default text replacement patterns (migrated from content filters) with negative IDs
- Fixed Reader screen navigation - onNavigate parameter properly passed through ReaderSettingMainLayout
- Default patterns initialized on first launch (IDs: -1 to -5)
- Default patterns marked with "Default" badge and cannot be edited/deleted
- Text replacements integrated in FindChapterById and SubscribeChapterById use cases
- Text replacements integrated in TTSContentLoaderImpl for TTS functionality
- Replacements applied BEFORE content filtering in the processing pipeline
- FIXED: TextReplacementUseCase now supports both regex patterns and literal string replacement
- FIXED: Optimized regex detection with companion object constant
- FIXED: Added missing NavigationRoutes import to ReaderSettingComposable
- FIXED: Removed caching from TextReplacementUseCase to ensure real-time updates when replacements change
- ADDED: Import/Export functionality for text replacements (JSON format)
- ADDED: Import/Export buttons in TextReplacementScreen TopAppBar
- ADDED: ImportDialog and ExportDialog composables

### Now
- Feature complete with import/export functionality
- All navigation and content update issues resolved

### Next
- User testing and feedback

## Open Questions
- None

## Working Set (files/ids/commands)
- domain/src/commonMain/kotlin/ireader/domain/usecases/reader/TextReplacementUseCase.kt (ADDED import/export methods)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/textreplacement/TextReplacementViewModel.kt (ADDED import/export methods)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/textreplacement/TextReplacementScreen.kt (ADDED import/export UI)
- presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ReaderSettingComposable.kt (FIXED NavigationRoutes import)
- domain/src/commonMain/kotlin/ireader/domain/usecases/local/chapter_usecases/SubscribeChapterById.kt
