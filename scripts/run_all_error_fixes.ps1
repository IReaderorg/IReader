# Master Script - Run All Error Fixes
# Executes all error fix scripts in the correct order

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IReader Compilation Error Fixes" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$startTime = Get-Date

# Step 1: Common compilation errors
Write-Host "[1/4] Running common compilation error fixes..." -ForegroundColor Yellow
& "$PSScriptRoot\fix_common_compilation_errors.ps1"
Write-Host ""

# Step 2: Critical errors
Write-Host "[2/4] Running critical error fixes..." -ForegroundColor Yellow
& "$PSScriptRoot\fix_critical_errors.ps1"
Write-Host ""

# Step 3: ScreenModel errors
Write-Host "[3/4] Running ScreenModel error fixes..." -ForegroundColor Yellow
& "$PSScriptRoot\fix_screenmodel_errors.ps1"
Write-Host ""

# Step 4: Remaining ViewModel errors
Write-Host "[4/4] Running remaining ViewModel error fixes..." -ForegroundColor Yellow
& "$PSScriptRoot\fix_remaining_viewmodel_errors.ps1"
Write-Host ""

$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All Fixes Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Time taken: $($duration.TotalSeconds) seconds" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Review the changes in your IDE" -ForegroundColor White
Write-Host "2. Test compilation (but don't run gradlew build as requested)" -ForegroundColor White
Write-Host "3. Check for any remaining errors" -ForegroundColor White
Write-Host ""
Write-Host "Files modified across all scripts:" -ForegroundColor Yellow
Write-Host "- ImageLoader.kt" -ForegroundColor White
Write-Host "- BookDetailScreenEnhanced.kt" -ForegroundColor White
Write-Host "- BookDetailScreenNew.kt" -ForegroundColor White
Write-Host "- BookDetailScreenRefactored.kt" -ForegroundColor White
Write-Host "- BookDetailScreenModel.kt" -ForegroundColor White
Write-Host "- ChapterDetailBottomBar.kt" -ForegroundColor White
Write-Host "- ChapterFilters.kt" -ForegroundColor White
Write-Host "- ChapterSort.kt" -ForegroundColor White
Write-Host "- AccessibilityUtils.kt" -ForegroundColor White
Write-Host "- AccessibleBookListItem.kt" -ForegroundColor White
Write-Host "- PerformantBookList.kt" -ForegroundColor White
Write-Host "- StateViewModel.kt" -ForegroundColor White
Write-Host "- DownloadScreenModel.kt" -ForegroundColor White
Write-Host "- DownloadScreens.kt" -ForegroundColor White
Write-Host "- MigrationScreenModel.kt" -ForegroundColor White
Write-Host "- MigrationScreens.kt" -ForegroundColor White
Write-Host "- LibraryViewModel.kt" -ForegroundColor White
Write-Host "- ExploreScreenEnhanced.kt" -ForegroundColor White
Write-Host "- GlobalSearchViewModel.kt (both versions)" -ForegroundColor White
Write-Host "- GlobalSearchScreen.kt" -ForegroundColor White
Write-Host "- ExtensionSecurityDialog.kt" -ForegroundColor White
Write-Host "- SourceDetailScreenEnhanced.kt" -ForegroundColor White
Write-Host "- GlobalSearchScreenEnhanced.kt" -ForegroundColor White
Write-Host "- SettingsAppearanceScreen.kt" -ForegroundColor White
Write-Host "- CloudBackupViewModel.kt" -ForegroundColor White
Write-Host "- GoogleDriveViewModel.kt" -ForegroundColor White
Write-Host "- BadgeStoreViewModel.kt" -ForegroundColor White
Write-Host "- SettingsNotificationViewModel.kt" -ForegroundColor White
Write-Host "- SettingsSecurityViewModel.kt" -ForegroundColor White
Write-Host "- AdvancedStatisticsScreen.kt" -ForegroundColor White
Write-Host "- EnhancedStatisticsScreen.kt" -ForegroundColor White
Write-Host "- StatisticsScreen.kt" -ForegroundColor White
Write-Host "- VoiceSelectionViewModel.kt" -ForegroundColor White
Write-Host "- DynamicColors.kt" -ForegroundColor White
Write-Host "- AppearanceToolbar.kt" -ForegroundColor White
Write-Host "- DownloaderTopAppBar.kt" -ForegroundColor White
Write-Host "- MigrationViewModel.kt" -ForegroundColor White
Write-Host "- FeaturePluginViewModel.kt" -ForegroundColor White
Write-Host "- TTSViewModel.kt" -ForegroundColor White
Write-Host "- BadgeManagementViewModel.kt" -ForegroundColor White
Write-Host "- NFTBadgeViewModel.kt" -ForegroundColor White
Write-Host ""
Write-Host "Total: ~43 files modified" -ForegroundColor Green
Write-Host ""
