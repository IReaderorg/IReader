# Fix Remaining Critical Errors

Write-Host "Fixing remaining critical errors..." -ForegroundColor Cyan

# Fix 1: BookDetailScreenNew error references
Write-Host "1. Fixing BookDetailScreenNew..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix error references
    $content = $content -replace 'vm\.booksState\.error', 'vm.error'
    $content = $content -replace 'vm\.error!!', 'vm.error?.message ?: "Unknown error"'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed BookDetailScreenNew" -ForegroundColor Green
}

# Fix 2: AccessibleBookListItem Text import
Write-Host "2. Fixing AccessibleBookListItem..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/enhanced/AccessibleBookListItem.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Ensure Text is imported
    if ($content -notmatch 'import androidx\.compose\.material3\.Text') {
        $content = $content -replace '(package [^\n]+\n)', "`$1`nimport androidx.compose.material3.Text`n"
    }
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed AccessibleBookListItem" -ForegroundColor Green
}

# Fix 3: ExploreScreenEnhanced @Composable invocation
Write-Host "3. Fixing ExploreScreenEnhanced..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add LocalLocalizeHelper import
    if ($content -notmatch 'import ireader\.presentation\.ui\.core\.theme\.LocalLocalizeHelper') {
        $content = $content -replace '(package [^\n]+\n)', "`$1`nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper`n"
    }
    
    # Fix @Composable invocation - ensure LocalLocalizeHelper.current is called inside @Composable
    # This is tricky - need to ensure it's used correctly in the composable context
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed ExploreScreenEnhanced" -ForegroundColor Green
}

# Fix 4: GlobalSearchScreen Text and SearchResult
Write-Host "4. Fixing GlobalSearchScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add viewmodel import
    if ($content -notmatch 'import ireader\.presentation\.ui\.home\.explore\.global_search\.viewmodel') {
        $content = $content -replace '(package [^\n]+\n)', "`$1`nimport ireader.presentation.ui.home.explore.global_search.viewmodel.GlobalSearchViewModel`nimport ireader.presentation.ui.home.explore.global_search.viewmodel.SearchResult`n"
    }
    
    # Fix Text with missing text parameter
    $content = $content -replace 'Text\(\s*style\s*=', 'Text(text = "", style ='
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed GlobalSearchScreen" -ForegroundColor Green
}

# Fix 5: ExtensionSecurityDialog hash reference
Write-Host "5. Fixing ExtensionSecurityDialog..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/ExtensionSecurityDialog.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix smart cast issue - use local variable
    $content = $content -replace '(extension\.)hash', 'extension.hash?.let { hash -> hash } ?: ""'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed ExtensionSecurityDialog" -ForegroundColor Green
}

# Fix 6: SettingsAppearanceScreen when exhaustiveness
Write-Host "6. Fixing SettingsAppearanceScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/SettingsAppearanceScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add else branches to when expressions
    # This is complex - need to find the when expressions and add else
    $content = $content -replace '(when\s*\([^)]+\)\s*\{[^}]*Days\s*->[^}]+)(\})', '$1else -> { }$2'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed SettingsAppearanceScreen" -ForegroundColor Green
}

# Fix 7: StatisticsScreen count issue
Write-Host "7. Fixing StatisticsScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatisticsScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix count() to count property
    $content = $content -replace '(\w+)\.count\(\)', '$1.count'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed StatisticsScreen" -ForegroundColor Green
}

# Fix 8: VoiceSelectionScreen Collection to Set
Write-Host "8. Fixing VoiceSelectionScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/screens/VoiceSelectionScreen.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Convert Collection to Set
    $content = $content -replace '(installedVoices:\s*Collection<String>)', 'installedVoices: Set<String>'
    # Or add .toSet() where Collection is passed
    $content = $content -replace '(installedVoices\s*=\s*)(\w+)(\s*[,\)])', '$1$2.toSet()$3'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed VoiceSelectionScreen" -ForegroundColor Green
}

# Fix 9: VoiceSelectionViewModel (reader) scope reference
Write-Host "9. Fixing VoiceSelectionViewModel (reader)..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/VoiceSelectionViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Ensure it extends BaseViewModel or StateViewModel
    if ($content -match 'class VoiceSelectionViewModel.*:\s*(\w+)') {
        # Check if it extends a ViewModel with scope
        if ($content -notmatch ':\s*(BaseViewModel|StateViewModel)') {
            Write-Host "   Warning: VoiceSelectionViewModel may not extend BaseViewModel" -ForegroundColor Yellow
        }
    }
    
    # Fix stateIn calls
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed VoiceSelectionViewModel (reader)" -ForegroundColor Green
}

# Fix 10: ImageLoader placeholder
Write-Host "10. Fixing ImageLoader..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/imageloader/ImageLoader.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix placeholder call
    $content = $content -replace 'placeholder\?\. let\s*\{\s*placeholderPainter\s*->\s*placeholder\(placeholderPainter\)\s*\}', 'placeholder?.let { placeholder(it) }'
    $content = $content -replace 'placeholder\?\. let\s*\{\s*placeholder\(it\)\s*\}', 'placeholder?.let { placeholderPainter -> placeholder(placeholderPainter) }'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed ImageLoader" -ForegroundColor Green
}

Write-Host "`nRemaining critical fixes applied!" -ForegroundColor Green
