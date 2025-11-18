# Comprehensive Fix Script for All Compilation Errors
# This script addresses all identified compilation errors systematically

Write-Host "=== IReader Compilation Error Fix Script ===" -ForegroundColor Cyan
Write-Host "Fixing all compilation errors..." -ForegroundColor Yellow

$FixCount = 0

Write-Host "`n[1] Fixing AccessibilityUtils.kt - Size and Ripple issues..." -ForegroundColor Yellow
$accessPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/accessibility/AccessibilityUtils.kt"
if (Test-Path $accessPath) {
    $content = Get-Content -Path $accessPath -Raw -Encoding UTF8
    $content = $content -replace '\.size\((\d+)\)(?!\.dp)', '.size($1.dp)'
    $content = $content -replace 'rememberRipple\(', 'ripple('
    Set-Content -Path $accessPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed size and ripple issues" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[2] Fixing AccessibleBookListItem.kt - Val reassignment..." -ForegroundColor Yellow
$bookListPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/enhanced/AccessibleBookListItem.kt"
if (Test-Path $bookListPath) {
    $content = Get-Content -Path $bookListPath -Raw -Encoding UTF8
    $content = $content -replace 'val\s+(contentDescription\s*=)', 'var $1'
    $content = $content -replace 'val\s+(accessibilityLabel\s*=)', 'var $1'
    Set-Content -Path $bookListPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed val reassignment issues" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[3] Fixing StateViewModel.kt - stateIn import..." -ForegroundColor Yellow
$stateVMPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/core/viewmodel/StateViewModel.kt"
if (Test-Path $stateVMPath) {
    $content = Get-Content -Path $stateVMPath -Raw -Encoding UTF8
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\..*\n)', "`$1import kotlinx.coroutines.flow.stateIn`n"
    }
    Set-Content -Path $stateVMPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Added stateIn import" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[4] Fixing CrashScreen.kt and DiagnosticsScreen.kt - Experimental API..." -ForegroundColor Yellow
$crashPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/crash/CrashScreen.kt"
$diagPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/diagnostics/DiagnosticsScreen.kt"
foreach ($path in @($crashPath, $diagPath)) {
    if (Test-Path $path) {
        $content = Get-Content -Path $path -Raw -Encoding UTF8
        if ($content -notmatch '@OptIn\(ExperimentalMaterial3Api::class\)') {
            $content = $content -replace '(@Composable\s+fun)', "@OptIn(ExperimentalMaterial3Api::class)`n`$1"
        }
        Set-Content -Path $path -Value $content -Encoding UTF8 -NoNewline
        $fileName = Split-Path $path -Leaf
        Write-Host "  Added OptIn annotation to $fileName" -ForegroundColor Green
        $FixCount++
    }
}

Write-Host "`n[5] Fixing DownloadScreenModel.kt - mutableState to updateState..." -ForegroundColor Yellow
$downloadModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreenModel.kt"
if (Test-Path $downloadModelPath) {
    $content = Get-Content -Path $downloadModelPath -Raw -Encoding UTF8
    $content = $content -replace 'mutableState\s*\{([^}]+)\}', 'updateState { $1 }'
    $content = $content -replace 'mutableState\s*=\s*mutableState', 'updateState { it'
    Set-Content -Path $downloadModelPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted mutableState to updateState" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[6] Fixing MigrationScreenModel.kt - mutableState to updateState..." -ForegroundColor Yellow
$migrationModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt"
if (Test-Path $migrationModelPath) {
    $content = Get-Content -Path $migrationModelPath -Raw -Encoding UTF8
    $content = $content -replace 'mutableState\s*\{([^}]+)\}', 'updateState { $1 }'
    $content = $content -replace 'mutableState\s*=\s*mutableState', 'updateState { it'
    Set-Content -Path $migrationModelPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted mutableState to updateState" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[7] Fixing StatsScreenModel.kt - mutableState to updateState..." -ForegroundColor Yellow
$statsModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatsScreenModel.kt"
if (Test-Path $statsModelPath) {
    $content = Get-Content -Path $statsModelPath -Raw -Encoding UTF8
    $content = $content -replace 'mutableState\s*\{([^}]+)\}', 'updateState { $1 }'
    $content = $content -replace 'mutableState\s*=\s*mutableState', 'updateState { it'
    Set-Content -Path $statsModelPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted mutableState to updateState" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[8] Fixing AppearanceToolbar.kt - Parameter names..." -ForegroundColor Yellow
$appearanceToolbarPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/AppearanceToolbar.kt"
if (Test-Path $appearanceToolbarPath) {
    $content = Get-Content -Path $appearanceToolbarPath -Raw -Encoding UTF8
    $content = $content -replace 'popBackStack\s*=', 'onPopBackStack ='
    Set-Content -Path $appearanceToolbarPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed parameter names" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[9] Fixing DownloaderTopAppBar.kt - Parameter names..." -ForegroundColor Yellow
$downloaderToolbarPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/downloader/DownloaderTopAppBar.kt"
if (Test-Path $downloaderToolbarPath) {
    $content = Get-Content -Path $downloaderToolbarPath -Raw -Encoding UTF8
    $content = $content -replace 'popBackStack\s*=', 'onPopBackStack ='
    Set-Content -Path $downloaderToolbarPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed parameter names" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n[10] Fixing count() function invocation errors..." -ForegroundColor Yellow
$countErrorPaths = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/EnhancedStatisticsScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatisticsScreen.kt"
)
foreach ($path in $countErrorPaths) {
    if (Test-Path $path) {
        $content = Get-Content -Path $path -Raw -Encoding UTF8
        $content = $content -replace '\.count(?!\()', '.count()'
        Set-Content -Path $path -Value $content -Encoding UTF8 -NoNewline
        $fileName = Split-Path $path -Leaf
        Write-Host "  Fixed count() in $fileName" -ForegroundColor Green
        $FixCount++
    }
}

Write-Host "`n=== Fix Summary ===" -ForegroundColor Cyan
Write-Host "Fixes applied: $FixCount" -ForegroundColor Green

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Run: .\gradlew :presentation:compileReleaseKotlinAndroid" -ForegroundColor White
Write-Host "2. Review any remaining errors" -ForegroundColor White
Write-Host "3. Check the error log for details" -ForegroundColor White
