# Fix Remaining Compilation Errors - Final Pass

Write-Host "=== Fixing Remaining Compilation Errors ===" -ForegroundColor Cyan

$FixCount = 0

# Fix 1: DownloadScreens.kt - Koin and Voyager imports
Write-Host "`n[1] Fixing DownloadScreens.kt..." -ForegroundColor Yellow
$downloadScreensPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreens.kt"
if (Test-Path $downloadScreensPath) {
    $content = Get-Content -Path $downloadScreensPath -Raw -Encoding UTF8
    
    # Add missing imports
    if ($content -notmatch 'import org\.koin\.compose\.getKoin') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.getKoin`nimport cafe.adriel.voyager.core.model.rememberScreenModel`n"
    }
    
    # Fix koin() to getKoin()
    $content = $content -replace 'val\s+koin\s*=\s*koin\(\)', 'val koin = getKoin()'
    
    # Fix getScreenModel to rememberScreenModel
    $content = $content -replace 'getScreenModel', 'rememberScreenModel'
    
    Set-Content -Path $downloadScreensPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed DownloadScreens.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 2: MigrationScreens.kt - Koin and Voyager imports
Write-Host "`n[2] Fixing MigrationScreens.kt..." -ForegroundColor Yellow
$migrationScreensPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt"
if (Test-Path $migrationScreensPath) {
    $content = Get-Content -Path $migrationScreensPath -Raw -Encoding UTF8
    
    # Add missing imports
    if ($content -notmatch 'import org\.koin\.compose\.getKoin') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.getKoin`nimport cafe.adriel.voyager.core.model.rememberScreenModel`n"
    }
    
    # Fix koin() to getKoin()
    $content = $content -replace 'val\s+koin\s*=\s*koin\(\)', 'val koin = getKoin()'
    
    # Fix getScreenModel to rememberScreenModel
    $content = $content -replace 'getScreenModel', 'rememberScreenModel'
    
    Set-Content -Path $migrationScreensPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed MigrationScreens.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 3: AdvancedStatisticsScreen.kt - Koin imports
Write-Host "`n[3] Fixing AdvancedStatisticsScreen.kt..." -ForegroundColor Yellow
$advStatsPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/AdvancedStatisticsScreen.kt"
if (Test-Path $advStatsPath) {
    $content = Get-Content -Path $advStatsPath -Raw -Encoding UTF8
    
    # Add missing imports
    if ($content -notmatch 'import org\.koin\.compose\.getKoin') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.getKoin`n"
    }
    
    # Fix koin() to getKoin()
    $content = $content -replace 'val\s+koin\s*=\s*koin\(\)', 'val koin = getKoin()'
    
    Set-Content -Path $advStatsPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed AdvancedStatisticsScreen.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 4: GlobalSearchScreen.kt - SearchResult and other issues
Write-Host "`n[4] Fixing GlobalSearchScreen.kt..." -ForegroundColor Yellow
$globalSearchPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchScreen.kt"
if (Test-Path $globalSearchPath) {
    $content = Get-Content -Path $globalSearchPath -Raw -Encoding UTF8
    
    # Add SearchResult import if missing
    if ($content -notmatch 'import ireader\.presentation\.ui\.home\.explore\.global_search\.viewmodel\.SearchResult') {
        $content = $content -replace '(package .*\n)', "`$1`nimport ireader.presentation.ui.home.explore.global_search.viewmodel.SearchResult`n"
    }
    
    Set-Content -Path $globalSearchPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed GlobalSearchScreen.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 5: GlobalSearchViewModel.kt - SearchResult definition
Write-Host "`n[5] Fixing GlobalSearchViewModel.kt..." -ForegroundColor Yellow
$globalSearchVMPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchViewModel.kt"
if (Test-Path $globalSearchVMPath) {
    $content = Get-Content -Path $globalSearchVMPath -Raw -Encoding UTF8
    
    # Add SearchResult data class if missing
    if ($content -notmatch 'data class SearchResult') {
        $searchResultDef = @'

data class SearchResult(
    val sourceId: Long,
    val sourceName: String,
    val books: List<Book>,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
'@
        $content = $content -replace '(package .*\n)', "`$1$searchResultDef`n"
    }
    
    Set-Content -Path $globalSearchVMPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed GlobalSearchViewModel.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 6: ExploreScreenEnhanced.kt - LocalizeHelper
Write-Host "`n[6] Fixing ExploreScreenEnhanced.kt..." -ForegroundColor Yellow
$exploreEnhancedPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt"
if (Test-Path $exploreEnhancedPath) {
    $content = Get-Content -Path $exploreEnhancedPath -Raw -Encoding UTF8
    
    # Add LocalizeHelper import if missing
    if ($content -notmatch 'import ireader\.presentation\.ui\.core\.theme\.LocalLocalizeHelper') {
        $content = $content -replace '(package .*\n)', "`$1`nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper`n"
    }
    
    Set-Content -Path $exploreEnhancedPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed ExploreScreenEnhanced.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 7: SourceDetailScreenEnhanced.kt and GlobalSearchScreenEnhanced.kt - String resources
Write-Host "`n[7] Fixing String resource references..." -ForegroundColor Yellow
$stringResourceFiles = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreenEnhanced.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/GlobalSearchScreenEnhanced.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenRefactored.kt"
)

foreach ($file in $stringResourceFiles) {
    if (Test-Path $file) {
        $content = Get-Content -Path $file -Raw -Encoding UTF8
        
        # Add string resource imports if missing
        if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
            $content = $content -replace '(import ireader\.i18n\.resources\.Res)', "`$1`nimport ireader.i18n.resources.*"
        }
        
        Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
        $fileName = Split-Path $file -Leaf
        Write-Host "  Fixed string resources in $fileName" -ForegroundColor Green
        $FixCount++
    }
}

# Fix 8: ExtensionSecurityDialog.kt - Smart cast issue
Write-Host "`n[8] Fixing ExtensionSecurityDialog.kt..." -ForegroundColor Yellow
$extSecurityPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/ExtensionSecurityDialog.kt"
if (Test-Path $extSecurityPath) {
    $content = Get-Content -Path $extSecurityPath -Raw -Encoding UTF8
    
    # Fix smart cast issue by using local variable
    $content = $content -replace '(signatureHash\s*->\s*\{)', 'signatureHash -> { val hash = signatureHash;'
    $content = $content -replace '(signatureHash)', 'hash'
    
    Set-Content -Path $extSecurityPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed ExtensionSecurityDialog.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 9: PrivacySettingsScreen.kt - Experimental API
Write-Host "`n[9] Fixing PrivacySettingsScreen.kt..." -ForegroundColor Yellow
$privacyPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/PrivacySettingsScreen.kt"
if (Test-Path $privacyPath) {
    $content = Get-Content -Path $privacyPath -Raw -Encoding UTF8
    
    if ($content -notmatch '@OptIn\(ExperimentalMaterial3Api::class\)') {
        $content = $content -replace '(@Composable\s+fun)', "@OptIn(ExperimentalMaterial3Api::class)`n`$1"
    }
    
    Set-Content -Path $privacyPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed PrivacySettingsScreen.kt" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n=== Fix Summary ===" -ForegroundColor Cyan
Write-Host "Fixes applied: $FixCount" -ForegroundColor Green

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Run: .\gradlew :presentation:compileReleaseKotlinAndroid" -ForegroundColor White
Write-Host "2. Review any remaining errors" -ForegroundColor White
