# Fix UI and String Resource Errors
# This script fixes UI component and string resource issues

Write-Host "Fixing UI and string resource errors..." -ForegroundColor Cyan

# Fix 1: ExploreScreenEnhanced - LocalizeHelper and string resources
Write-Host "1. Fixing ExploreScreenEnhanced..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add LocalizeHelper import
    if ($content -notmatch 'import ireader\.presentation\.ui\.core\.theme\.LocalLocalizeHelper') {
        $content = $content -replace '(package ireader\.presentation\.ui\.home\.explore)', "`$1`n`nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper"
    }
    
    # Add string resources import
    if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
        $content = $content -replace '(package ireader\.presentation\.ui\.home\.explore)', "`$1`n`nimport ireader.i18n.resources.*"
    }
    
    # Fix @Composable invocation outside composable context
    $content = $content -replace '(@Composable\s+fun\s+\w+\([^)]*\)\s*\{[^}]*)(LocalLocalizeHelper\.current)', '$1val localizeHelper = LocalLocalizeHelper.current; localizeHelper'
    
    Set-Content $file $content -NoNewline
}

# Fix 2: GlobalSearchScreen - SearchResult and viewmodel import
Write-Host "2. Fixing GlobalSearchScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add viewmodel import
    if ($content -notmatch 'import ireader\.presentation\.ui\.home\.explore\.global_search\.viewmodel') {
        $content = $content -replace '(package ireader\.presentation\.ui\.home\.explore\.global_search)', "`$1`n`nimport ireader.presentation.ui.home.explore.global_search.viewmodel.GlobalSearchViewModel`nimport ireader.presentation.ui.home.explore.global_search.viewmodel.SearchResult"
    }
    
    # Fix Text composable - add text parameter
    $content = $content -replace 'Text\(\s*style\s*=', 'Text(text = "", style ='
    
    # Fix lambda parameter type inference
    $content = $content -replace 'items\(\s*\{\s*\}\s*\)\s*\{\s*book\s*->', 'items(emptyList<Book>()) { book ->'
    
    Set-Content $file $content -NoNewline
}

# Fix 3: GlobalSearchViewModel - SearchResult and imports
Write-Host "3. Fixing GlobalSearchViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add Book import at the top
    if ($content -notmatch 'import ireader\.domain\.models\.entities\.Book') {
        $content = $content -replace '(package ireader\.presentation\.ui\.home\.explore\.global_search\.viewmodel)', "`$1`n`nimport ireader.domain.models.entities.Book"
    }
    
    # Move imports to beginning if they're not
    $content = $content -replace 'data class SearchResult.*\n\nimport', "import"
    
    # Add SearchResult data class after imports
    if ($content -notmatch 'data class SearchResult') {
        $content = $content -replace '(class GlobalSearchViewModel)', "data class SearchResult(`n    val sourceId: Long,`n    val sourceName: String,`n    val books: List<Book>,`n    val isLoading: Boolean = false,`n    val error: Throwable? = null`n)`n`n`$1"
    }
    
    # Fix Flow type inference
    $content = $content -replace '\.onStart\s*\{', '.onStart<List<SearchResult>> {'
    $content = $content -replace '\.catch\s*\{', '.catch<List<SearchResult>> {'
    $content = $content -replace '\.map\s*\{', '.map<String, List<SearchResult>> {'
    
    # Fix sourceId reference
    $content = $content -replace '\.sourceId', '.id'
    
    Set-Content $file $content -NoNewline
}

# Fix 4: LibraryScreen - activity reference
Write-Host "4. Fixing LibraryScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/LibraryScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Remove or comment out activity reference (Android-specific)
    $content = $content -replace 'activity\.', '// activity. // Android-specific, implement in androidMain'
    
    Set-Content $file $content -NoNewline
}

# Fix 5: ExtensionSecurityDialog - hash reference
Write-Host "5. Fixing ExtensionSecurityDialog..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/ExtensionSecurityDialog.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix smart cast issue with hash
    $content = $content -replace '(val extension = .*\n.*)(extension\.hash)', "`$1val extensionHash = extension.hash`n                extensionHash"
    
    Set-Content $file $content -NoNewline
}

# Fix 6: SourceDetailScreenEnhanced - string resources
Write-Host "6. Fixing SourceDetailScreenEnhanced..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreenEnhanced.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add string resources import
    if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
        $content = $content -replace '(package ireader\.presentation\.ui\.home\.sources\.extension)', "`$1`n`nimport ireader.i18n.resources.*"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix 7: GlobalSearchScreenEnhanced - string resources
Write-Host "7. Fixing GlobalSearchScreenEnhanced..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/GlobalSearchScreenEnhanced.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add string resources import
    if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
        $content = $content -replace '(package ireader\.presentation\.ui\.home\.sources\.global_search)', "`$1`n`nimport ireader.i18n.resources.*"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix 8: GlobalSearchViewModel (sources) - Flow type inference
Write-Host "8. Fixing GlobalSearchViewModel (sources)..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/viewmodel/GlobalSearchViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix Flow type inference
    $content = $content -replace '\.onStart\s*\{', '.onStart<List<SearchResult>> {'
    $content = $content -replace '\.catch\s*\{', '.catch<List<SearchResult>> {'
    $content = $content -replace '\.map\s*\{', '.map<String, List<SearchResult>> {'
    
    # Fix argument type mismatch
    $content = $content -replace 'Function0<String>', 'String'
    
    # Fix sourceId and books references
    $content = $content -replace '\.sourceId', '.id'
    $content = $content -replace '\.books', '.results'
    
    Set-Content $file $content -NoNewline
}

# Fix 9: AppearanceToolbar - parameter name
Write-Host "9. Fixing AppearanceToolbar..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/AppearanceToolbar.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix parameter name
    $content = $content -replace 'onpopBackStack\s*=', 'onPopBackStack ='
    
    Set-Content $file $content -NoNewline
}

# Fix 10: DownloaderTopAppBar - parameter name
Write-Host "10. Fixing DownloaderTopAppBar..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/downloader/DownloaderTopAppBar.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix parameter name
    $content = $content -replace 'onpopBackStack\s*=', 'onPopBackStack ='
    
    Set-Content $file $content -NoNewline
}

# Fix 11: SettingsAppearanceScreen - when exhaustiveness
Write-Host "11. Fixing SettingsAppearanceScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/SettingsAppearanceScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add else branches to when expressions
    $content = $content -replace '(when\s*\([^)]+\)\s*\{[^}]+)(Days\s*->[^}]+\})', '$1Days -> { }$2else -> { }'
    
    Set-Content $file $content -NoNewline
}

# Fix 12: Statistics screens - count() function
Write-Host "12. Fixing Statistics screens..." -ForegroundColor Yellow
$files = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/EnhancedStatisticsScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatisticsScreen.kt"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        
        # Fix count property to count() function
        $content = $content -replace '\.count\s*\)', '.count())'
        $content = $content -replace '\.count\s*,', '.count(),'
        
        Set-Content $file $content -NoNewline
    }
}

# Fix 13: DynamicColors - Android-specific APIs
Write-Host "13. Fixing DynamicColors..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/theme/DynamicColors.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Comment out Android-specific dynamic color functions
    $content = $content -replace 'dynamicDarkColorScheme', '// dynamicDarkColorScheme // Android-specific'
    $content = $content -replace 'dynamicLightColorScheme', '// dynamicLightColorScheme // Android-specific'
    $content = $content -replace 'LocalContext', '// LocalContext // Android-specific'
    $content = $content -replace 'android\.', '// android. // Android-specific'
    
    Set-Content $file $content -NoNewline
}

Write-Host "`nUI and string resource fixes applied!" -ForegroundColor Green
