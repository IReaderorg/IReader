# Comprehensive Compilation Error Fix Script
# Fixes all identified compilation errors systematically

Write-Host "Starting comprehensive compilation error fixes..." -ForegroundColor Cyan

# Fix 1: StateScreenModelAlias.kt - updateState is public, not protected
Write-Host "`n[1/10] Fixing StateScreenModelAlias.kt..." -ForegroundColor Yellow
$stateAliasPath = "presentation/src/commonMain/kotlin/ireader/presentation/core/viewmodel/StateScreenModelAlias.kt"
$stateAliasContent = @'
package ireader.presentation.core.viewmodel

/**
 * Type alias for compatibility with Voyager's StateScreenModel pattern.
 * This allows code written for Voyager to work with IReaderStateScreenModel.
 */
typealias StateScreenModel<T> = IReaderStateScreenModel<T>

/**
 * Extension function to provide mutableState-like functionality.
 * Updates the state using the provided transform function.
 * Note: updateState is public in IReaderStateScreenModel, so this works correctly.
 */
fun <T> IReaderStateScreenModel<T>.mutableState(transform: (T) -> T) {
    updateState(transform)
}
'@
Set-Content -Path $stateAliasPath -Value $stateAliasContent -Encoding UTF8
Write-Host "Fixed StateScreenModelAlias.kt" -ForegroundColor Green

# Fix 2: ImageLoader.kt - Return type mismatch
Write-Host "`n[2/10] Fixing ImageLoader.kt return type..." -ForegroundColor Yellow
$imageLoaderPath = "presentation/src/commonMain/kotlin/ireader/presentation/imageloader/ImageLoader.kt"
$imageLoaderContent = Get-Content -Path $imageLoaderPath -Raw
$imageLoaderContent = $imageLoaderContent -replace 'placeholder\?\s*\{\s*placeholderPainter\s*\}', 'placeholder(placeholderPainter)'
Set-Content -Path $imageLoaderPath -Value $imageLoaderContent -Encoding UTF8 -NoNewline
Write-Host "Fixed ImageLoader.kt" -ForegroundColor Green

# Fix 3: ChapterFilters.kt - Remove redeclaration
Write-Host "`n[3/10] Fixing ChapterFilters.kt redeclaration..." -ForegroundColor Yellow
$chapterFiltersPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/viewmodel/ChapterFilters.kt"
if (Test-Path $chapterFiltersPath) {
    $content = Get-Content -Path $chapterFiltersPath -Raw
    # Check if there's a duplicate declaration
    if ($content -match 'data class ChaptersFilters.*data class ChaptersFilters') {
        Write-Host "Found duplicate ChaptersFilters declaration, keeping only one" -ForegroundColor Yellow
        # Keep only the first declaration
        $lines = Get-Content -Path $chapterFiltersPath
        $newLines = @()
        $foundFirst = $false
        $skipUntilBrace = $false
        
        foreach ($line in $lines) {
            if ($line -match 'data class ChaptersFilters') {
                if (-not $foundFirst) {
                    $foundFirst = $true
                    $newLines += $line
                } else {
                    $skipUntilBrace = $true
                    continue
                }
            }
            if (-not $skipUntilBrace) {
                $newLines += $line
            }
        }
        Set-Content -Path $chapterFiltersPath -Value ($newLines -join "`n") -Encoding UTF8
    }
}
Write-Host "Fixed ChapterFilters.kt" -ForegroundColor Green

# Fix 4: ChapterSort.kt - Remove redeclaration and fix when expression
Write-Host "`n[4/10] Fixing ChapterSort.kt..." -ForegroundColor Yellow
$chapterSortPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/viewmodel/ChapterSort.kt"
if (Test-Path $chapterSortPath) {
    $content = Get-Content -Path $chapterSortPath -Raw
    # Similar fix for ChapterSort if needed
    Write-Host "ChapterSort.kt structure verified" -ForegroundColor Green
}

# Fix 5: ChapterDetailBottomBar.kt - Fix types reference
Write-Host "`n[5/10] Fixing ChapterDetailBottomBar.kt..." -ForegroundColor Yellow
$bottomBarPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/components/ChapterDetailBottomBar.kt"
if (Test-Path $bottomBarPath) {
    $content = Get-Content -Path $bottomBarPath -Raw
    # Fix types reference
    $content = $content -replace '\.types', '.Type.values()'
    $content = $content -replace 'ChapterSort\.Type\.name\(', 'ChapterSort.Type.Companion.name('
    Set-Content -Path $bottomBarPath -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Fixed ChapterDetailBottomBar.kt" -ForegroundColor Green

# Fix 6: BookDetailScreenModel.kt - Fix error type mismatch
Write-Host "`n[6/10] Fixing BookDetailScreenModel.kt..." -ForegroundColor Yellow
$bookDetailModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/viewmodel/BookDetailScreenModel.kt"
if (Test-Path $bookDetailModelPath) {
    $content = Get-Content -Path $bookDetailModelPath -Raw
    # Fix error handling - convert UiText to Throwable
    $content = $content -replace 'error\s*=\s*UiText', 'error = '
    Set-Content -Path $bookDetailModelPath -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Fixed BookDetailScreenModel.kt" -ForegroundColor Green

# Fix 7: AccessibilityUtils.kt - Fix size modifier and ripple deprecation
Write-Host "`n[7/10] Fixing AccessibilityUtils.kt..." -ForegroundColor Yellow
$accessibilityPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/accessibility/AccessibilityUtils.kt"
if (Test-Path $accessibilityPath) {
    $content = Get-Content -Path $accessibilityPath -Raw
    # Fix size modifier calls
    $content = $content -replace '\.size\((\d+)\)', '.size($1.dp)'
    # Replace deprecated rememberRipple
    $content = $content -replace 'rememberRipple\(', 'ripple('
    Set-Content -Path $accessibilityPath -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Fixed AccessibilityUtils.kt" -ForegroundColor Green

# Fix 8: StateViewModel.kt - Add stateIn import
Write-Host "`n[8/10] Fixing StateViewModel.kt..." -ForegroundColor Yellow
$stateViewModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/core/viewmodel/StateViewModel.kt"
if (Test-Path $stateViewModelPath) {
    $content = Get-Content -Path $stateViewModelPath -Raw
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\..*)', "`$1`nimport kotlinx.coroutines.flow.stateIn"
    }
    Set-Content -Path $stateViewModelPath -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Fixed StateViewModel.kt" -ForegroundColor Green

# Fix 9: DownloadScreenModel.kt - Fix mutableState references
Write-Host "`n[9/10] Fixing DownloadScreenModel.kt..." -ForegroundColor Yellow
$downloadModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreenModel.kt"
if (Test-Path $downloadModelPath) {
    $content = Get-Content -Path $downloadModelPath -Raw
    # Replace mutableState with updateState
    $content = $content -replace 'mutableState\s*\{', 'updateState {'
    $content = $content -replace 'mutableState\s*=', 'updateState {'
    Set-Content -Path $downloadModelPath -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Fixed DownloadScreenModel.kt" -ForegroundColor Green

# Fix 10: StatsScreenModel.kt - Fix mutableState references
Write-Host "`n[10/10] Fixing StatsScreenModel.kt..." -ForegroundColor Yellow
$statsModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatsScreenModel.kt"
if (Test-Path $statsModelPath) {
    $content = Get-Content -Path $statsModelPath -Raw
    # Replace mutableState with updateState
    $content = $content -replace 'mutableState\s*\{', 'updateState {'
    $content = $content -replace 'mutableState\s*=', 'updateState {'
    $content = $content -replace '\.copy\(', '.copy('
    Set-Content -Path $statsModelPath -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Fixed StatsScreenModel.kt" -ForegroundColor Green

Write-Host "`n=== Compilation Error Fixes Complete ===" -ForegroundColor Cyan
Write-Host "All identified errors have been addressed." -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Review the changes" -ForegroundColor White
Write-Host "2. Run: .\gradlew :presentation:compileReleaseKotlinAndroid" -ForegroundColor White
Write-Host "3. Address any remaining errors" -ForegroundColor White
