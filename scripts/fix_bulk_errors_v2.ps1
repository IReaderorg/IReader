# Fix Bulk Compilation Errors
Write-Host "Fixing bulk errors..." -ForegroundColor Cyan

# 1. ImageLoader
Write-Host "1. ImageLoader..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/imageloader/ImageLoader.kt"
if (Test-Path $file) {
    (Get-Content $file -Raw) -replace 'placeholder\?\.\s*let\s*\{\s*\w+\s*->\s*placeholder\([^)]+\)\s*\}', 'placeholder?.let { placeholder(it) }' | Set-Content $file -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 2. BookDetailScreenNew
Write-Host "2. BookDetailScreenNew..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    $c = $c -replace 'vm\.booksState\.error', 'vm.error'
    $c = $c -replace 'vm\.error\s*!!', '(vm.error?.message ?: "Unknown error")'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 3. AccessibilityUtils
Write-Host "3. AccessibilityUtils..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/accessibility/AccessibilityUtils.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    if ($c -notmatch 'import androidx\.compose\.material\.ripple\.ripple') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport androidx.compose.material.ripple.ripple"
    }
    if ($c -notmatch 'import androidx\.compose\.ui\.unit\.dp') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport androidx.compose.ui.unit.dp"
    }
    $c = $c -replace '\.size\((\d+)\)(?!\.dp)', '.size($1.dp)'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 4. AccessibleBookListItem
Write-Host "4. AccessibleBookListItem..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/enhanced/AccessibleBookListItem.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    if ($c -notmatch 'import androidx\.compose\.material3\.Text') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport androidx.compose.material3.Text"
    }
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 5. ExploreScreenEnhanced
Write-Host "5. ExploreScreenEnhanced..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    if ($c -notmatch 'import ireader\.presentation\.ui\.core\.theme\.LocalLocalizeHelper') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper"
    }
    if ($c -notmatch 'import ireader\.i18n\.resources\.\*') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport ireader.i18n.resources.*"
    }
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 6. GlobalSearchScreen
Write-Host "6. GlobalSearchScreen..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchScreen.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    $c = $c -replace 'import ireader\.presentation\.ui\.home\.explore\.global_search\.viewmodel', 'import ireader.presentation.ui.home.explore.global_search'
    if ($c -notmatch 'import ireader\.presentation\.ui\.home\.explore\.global_search\.GlobalSearchViewModel') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport ireader.presentation.ui.home.explore.global_search.GlobalSearchViewModel`nimport ireader.presentation.ui.home.explore.global_search.SearchResult"
    }
    $c = $c -replace 'Text\(\s*style\s*=', 'Text(text = "", style ='
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 7. GlobalSearchViewModel explore
Write-Host "7. GlobalSearchViewModel explore..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchViewModel.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    $c = $c -replace '\.id\b', '.sourceId'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 8. LibraryViewModel
Write-Host "8. LibraryViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    $c = $c -replace '(_activeFilters)\.stateIn\(scope\)', '$1.asStateFlow()'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 9. ExtensionSecurityDialog
Write-Host "9. ExtensionSecurityDialog..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/ExtensionSecurityDialog.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    $c = $c -replace '(extension)\.hash', 'extension.hash ?: ""'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 10-11. String resources
Write-Host "10-11. String resources..." -ForegroundColor Yellow
$files = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreenEnhanced.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/GlobalSearchScreenEnhanced.kt"
)
foreach ($file in $files) {
    if (Test-Path $file) {
        $c = Get-Content $file -Raw
        if ($c -notmatch 'import ireader\.i18n\.resources\.\*') {
            $c = $c -replace '(package [^\n]+)', "`$1`nimport ireader.i18n.resources.*"
        }
        Set-Content $file $c -NoNewline
    }
}
Write-Host "   Done" -ForegroundColor Green

# 12. GlobalSearchViewModel sources
Write-Host "12. GlobalSearchViewModel sources..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/viewmodel/GlobalSearchViewModel.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    if ($c -notmatch 'import ireader\.domain\.models\.entities\.Book') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport ireader.domain.models.entities.Book"
    }
    $c = $c -replace '\.id\b', '.sourceId'
    $c = $c -replace '\.results\b', '.books'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

# 13. MigrationScreenModel
Write-Host "13. MigrationScreenModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt"
if (Test-Path $file) {
    $c = Get-Content $file -Raw
    if ($c -notmatch 'import ireader\.domain\.models\.migration\.MigrationBook') {
        $c = $c -replace '(package [^\n]+)', "`$1`nimport ireader.domain.models.migration.MigrationBook`nimport ireader.domain.models.migration.MigrationFlags`nimport ireader.domain.models.migration.MigrationSource"
    }
    $c = $c -replace 'getFavoriteBooksFlow', 'getLibraryBooks'
    Set-Content $file $c -NoNewline
    Write-Host "   Done" -ForegroundColor Green
}

Write-Host "`nBulk fixes completed!" -ForegroundColor Green
