# ScreenModel and ViewModel Error Fixes
# Fixes errors in DownloadScreenModel, MigrationScreenModel, and related files

Write-Host "Starting ScreenModel error fixes..." -ForegroundColor Cyan

$filesFixed = 0

# Fix 1: StateViewModel.kt - Add stateIn import and fix suspend call
Write-Host "`n1. Fixing StateViewModel..." -ForegroundColor Yellow
$stateViewModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/core/viewmodel/StateViewModel.kt"
if (Test-Path $stateViewModelPath) {
    $content = Get-Content $stateViewModelPath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Fix stateIn call - wrap in scope.launch or make function suspend
    $content = $content -replace '(fun\s+<T>\s+Flow<T>\.asStateFlow\(\):?\s*StateFlow<T>\s*=\s*)stateIn\(scope\)', 'suspend fun <T> Flow<T>.asStateFlow(): StateFlow<T> = stateIn(scope)'
    
    Set-Content $stateViewModelPath $content -NoNewline
    Write-Host "  Fixed: StateViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 2: DownloadScreenModel.kt - Fix Flow operations and catch syntax
Write-Host "`n2. Fixing DownloadScreenModel..." -ForegroundColor Yellow
$downloadScreenModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreenModel.kt"
if (Test-Path $downloadScreenModelPath) {
    $content = Get-Content $downloadScreenModelPath -Raw
    
    # Add missing imports
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.catch') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1catch`nimport kotlinx.coroutines.flow."
    }
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.onEach') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1onEach`nimport kotlinx.coroutines.flow."
    }
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.launchIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1launchIn`nimport kotlinx.coroutines.flow."
    }
    
    # Fix catch syntax - change .catch(e) to .catch { e ->
    $content = $content -replace '\.catch\(e\)\s*->', '.catch { e ->'
    
    # Fix mutableState references
    $content = $content -replace 'mutableState\.value\s*=\s*mutableState\.value\.copy', 'updateState { it.copy'
    $content = $content -replace '\)\s*\}(\s*\n\s*\})', ')$1'
    
    # Remove 'private' modifier from local functions
    $content = $content -replace 'private\s+fun\s+observeDownloads', 'fun observeDownloads'
    $content = $content -replace 'private\s+fun\s+loadCacheInfo', 'fun loadCacheInfo'
    $content = $content -replace 'private\s+fun\s+observeCacheSize', 'fun observeCacheSize'
    
    Set-Content $downloadScreenModelPath $content -NoNewline
    Write-Host "  Fixed: DownloadScreenModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 3: MigrationScreenModel.kt - Fix Flow operations and catch syntax
Write-Host "`n3. Fixing MigrationScreenModel..." -ForegroundColor Yellow
$migrationScreenModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt"
if (Test-Path $migrationScreenModelPath) {
    $content = Get-Content $migrationScreenModelPath -Raw
    
    # Add missing imports
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.catch') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1catch`nimport kotlinx.coroutines.flow."
    }
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.onEach') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1onEach`nimport kotlinx.coroutines.flow."
    }
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.launchIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1launchIn`nimport kotlinx.coroutines.flow."
    }
    
    # Fix catch syntax
    $content = $content -replace '\.catch\(e\)\s*->', '.catch { e ->'
    
    # Fix mutableState references
    $content = $content -replace 'mutableState\.value\s*=\s*mutableState\.value\.copy', 'updateState { it.copy'
    
    # Remove 'private' modifier from local functions
    $content = $content -replace 'private\s+fun\s+loadMigrationSources', 'fun loadMigrationSources'
    $content = $content -replace 'private\s+fun\s+observeMigrationProgress', 'fun observeMigrationProgress'
    
    Set-Content $migrationScreenModelPath $content -NoNewline
    Write-Host "  Fixed: MigrationScreenModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 4: DownloadScreens.kt - Fix IReaderScaffold and component references
Write-Host "`n4. Fixing DownloadScreens..." -ForegroundColor Yellow
$downloadScreensPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreens.kt"
if (Test-Path $downloadScreensPath) {
    $content = Get-Content $downloadScreensPath -Raw
    
    # Add missing imports
    if ($content -notmatch 'import ireader\.presentation\.ui\.core\.ui\.Scaffold') {
        $content = $content -replace '(package ireader\.presentation\.ui\.download)', "`$1`nimport ireader.presentation.ui.core.ui.Scaffold as IReaderScaffold`nimport ireader.presentation.ui.core.ui.TopAppBar as IReaderTopAppBar`nimport ireader.presentation.ui.core.ui.LoadingScreen as IReaderLoadingScreen"
    }
    
    Set-Content $downloadScreensPath $content -NoNewline
    Write-Host "  Fixed: DownloadScreens.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 5: MigrationScreens.kt - Fix IReaderScaffold and component references
Write-Host "`n5. Fixing MigrationScreens..." -ForegroundColor Yellow
$migrationScreensPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt"
if (Test-Path $migrationScreensPath) {
    $content = Get-Content $migrationScreensPath -Raw
    
    # Add missing imports
    if ($content -notmatch 'import ireader\.presentation\.ui\.core\.ui\.Scaffold') {
        $content = $content -replace '(package ireader\.presentation\.ui\.migration)', "`$1`nimport ireader.presentation.ui.core.ui.Scaffold as IReaderScaffold`nimport ireader.presentation.ui.core.ui.TopAppBar as IReaderTopAppBar`nimport ireader.presentation.ui.core.ui.LoadingScreen as IReaderLoadingScreen`nimport ireader.presentation.ui.core.ui.ErrorScreen as IReaderErrorScreen"
    }
    
    Set-Content $migrationScreensPath $content -NoNewline
    Write-Host "  Fixed: MigrationScreens.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 6: LibraryViewModel.kt - Fix stateIn call
Write-Host "`n6. Fixing LibraryViewModel..." -ForegroundColor Yellow
$libraryViewModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt"
if (Test-Path $libraryViewModelPath) {
    $content = Get-Content $libraryViewModelPath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Fix stateIn call - add SharingStarted parameter
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    # Add SharingStarted import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1SharingStarted`nimport kotlinx.coroutines.flow."
    }
    
    Set-Content $libraryViewModelPath $content -NoNewline
    Write-Host "  Fixed: LibraryViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 7: ExploreScreenEnhanced.kt - Fix LocalizeHelper and string resources
Write-Host "`n7. Fixing ExploreScreenEnhanced..." -ForegroundColor Yellow
$exploreScreenPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt"
if (Test-Path $exploreScreenPath) {
    $content = Get-Content $exploreScreenPath -Raw
    
    # Add LocalizeHelper import
    if ($content -notmatch 'import ireader\.presentation\.ui\.core\.theme\.LocalLocalizeHelper') {
        $content = $content -replace '(import ireader\.presentation)', "`$1`nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper"
    }
    
    # Add string resource imports
    if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
        $content = $content -replace '(import ireader\.i18n\.resources\.Res)', "`$1`nimport ireader.i18n.resources.*"
    }
    
    Set-Content $exploreScreenPath $content -NoNewline
    Write-Host "  Fixed: ExploreScreenEnhanced.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 8: GlobalSearchViewModel.kt - Add SearchResult class and fix Flow operations
Write-Host "`n8. Fixing GlobalSearchViewModel..." -ForegroundColor Yellow
$globalSearchViewModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchViewModel.kt"
if (Test-Path $globalSearchViewModelPath) {
    $content = Get-Content $globalSearchViewModelPath -Raw
    
    # Add SearchResult data class if missing
    if ($content -notmatch 'data class SearchResult') {
        $searchResultClass = @"

data class SearchResult(
    val sourceId: Long,
    val sourceName: String,
    val books: List<Book>,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
"@
        # Add after imports
        $content = $content -replace '(class GlobalSearchViewModel)', "$searchResultClass`n`n`$1"
    }
    
    # Add missing imports
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.onStart') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1onStart`nimport kotlinx.coroutines.flow."
    }
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.map') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1map`nimport kotlinx.coroutines.flow."
    }
    
    Set-Content $globalSearchViewModelPath $content -NoNewline
    Write-Host "  Fixed: GlobalSearchViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 9: GlobalSearchScreen.kt - Fix Text import and SearchResult
Write-Host "`n9. Fixing GlobalSearchScreen..." -ForegroundColor Yellow
$globalSearchScreenPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchScreen.kt"
if (Test-Path $globalSearchScreenPath) {
    $content = Get-Content $globalSearchScreenPath -Raw
    
    # Add Text import if missing
    if ($content -notmatch 'import androidx\.compose\.material3\.Text') {
        $content = $content -replace '(import androidx\.compose\.material3\.)', "`$1Text`nimport androidx.compose.material3."
    }
    
    Set-Content $globalSearchScreenPath $content -NoNewline
    Write-Host "  Fixed: GlobalSearchScreen.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 10: ExtensionSecurityDialog.kt - Fix hash smart cast
Write-Host "`n10. Fixing ExtensionSecurityDialog..." -ForegroundColor Yellow
$extensionSecurityPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/ExtensionSecurityDialog.kt"
if (Test-Path $extensionSecurityPath) {
    $content = Get-Content $extensionSecurityPath -Raw
    
    # Fix smart cast by using local variable
    $content = $content -replace '(extension\.hash)', 'extension.hash?.let { hash -> hash }'
    
    Set-Content $extensionSecurityPath $content -NoNewline
    Write-Host "  Fixed: ExtensionSecurityDialog.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 11: SourceDetailScreenEnhanced.kt and GlobalSearchScreenEnhanced.kt - Add string resources
Write-Host "`n11. Fixing Source and GlobalSearch screens..." -ForegroundColor Yellow
$sourceDetailPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreenEnhanced.kt"
$globalSearchEnhancedPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/GlobalSearchScreenEnhanced.kt"

foreach ($path in @($sourceDetailPath, $globalSearchEnhancedPath)) {
    if (Test-Path $path) {
        $content = Get-Content $path -Raw
        
        # Add string resource imports
        if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
            $content = $content -replace '(import ireader\.i18n\.resources\.Res)', "`$1`nimport ireader.i18n.resources.*"
        }
        
        Set-Content $path $content -NoNewline
        Write-Host "  Fixed: $(Split-Path $path -Leaf)" -ForegroundColor Green
        $filesFixed++
    }
}

# Fix 12: SettingsAppearanceScreen.kt - Add else branches to when expressions
Write-Host "`n12. Fixing SettingsAppearanceScreen..." -ForegroundColor Yellow
$settingsAppearancePath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/SettingsAppearanceScreen.kt"
if (Test-Path $settingsAppearancePath) {
    $content = Get-Content $settingsAppearancePath -Raw
    
    # Add else branches to when expressions (simple approach - add else -> Unit)
    $content = $content -replace '(when\s*\([^)]*\)\s*\{[^}]*ChapterSort\.Type\.[^}]*)\}', '$1    else -> Unit}'
    
    Set-Content $settingsAppearancePath $content -NoNewline
    Write-Host "  Fixed: SettingsAppearanceScreen.kt" -ForegroundColor Green
    $filesFixed++
}

Write-Host "`n=== Fix Summary ===" -ForegroundColor Cyan
Write-Host "Total files fixed: $filesFixed" -ForegroundColor Green
Write-Host "`nPlease review the changes and test compilation." -ForegroundColor Yellow
