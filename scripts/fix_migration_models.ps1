# Fix Migration Models - Use Domain Models Instead of Presentation Duplicates

Write-Host "Fixing Migration models to use domain layer..." -ForegroundColor Cyan

# Fix 1: Remove presentation layer MigrationFlags and MigrationSource from MigrationScreens.kt
Write-Host "1. Updating MigrationScreens.kt to use domain models..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add domain model imports
    if ($content -notmatch 'import ireader\.domain\.models\.migration\.MigrationFlags') {
        $content = $content -replace '(import org\.koin\.compose\.koinInject)', "import ireader.domain.models.migration.MigrationFlags`nimport ireader.domain.models.migration.MigrationSource`n`$1"
    }
    
    # Remove presentation layer enum and data class definitions
    $content = $content -replace 'enum class MigrationFlags\(val value: Int\) \{[^}]+\}', '// Using domain MigrationFlags'
    $content = $content -replace 'data class MigrationSource\([^)]+\)', '// Using domain MigrationSource'
    
    # Fix property references to match domain model
    # Domain model likely has different property names
    $content = $content -replace '\.migrateChapters', '.chapters'
    $content = $content -replace '\.migrateBookmarks', '.bookmarks'
    $content = $content -replace '\.migrateCategories', '.categories'
    $content = $content -replace '\.migrateCustomCover', '.customCover'
    $content = $content -replace '\.migrateReadingProgress', '.readingProgress'
    
    # Fix copy function calls
    $content = $content -replace '\.copy\(migrateChapters', '.copy(chapters'
    $content = $content -replace '\.copy\(migrateBookmarks', '.copy(bookmarks'
    $content = $content -replace '\.copy\(migrateCategories', '.copy(categories'
    $content = $content -replace '\.copy\(migrateCustomCover', '.copy(customCover'
    $content = $content -replace '\.copy\(migrateReadingProgress', '.copy(readingProgress'
    
    # Fix WideNavigationRailValue condition
    $content = $content -replace 'if \(!wideNavigationRail\)', 'if (wideNavigationRail == WideNavigationRailValue.Disabled)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Updated MigrationScreens.kt" -ForegroundColor Green
}

# Fix 2: Update MigrationScreenModel.kt
Write-Host "2. Updating MigrationScreenModel.kt..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add domain model imports
    if ($content -notmatch 'import ireader\.domain\.models\.migration') {
        $content = $content -replace '(import kotlinx\.coroutines)', "import ireader.domain.models.migration.MigrationFlags`nimport ireader.domain.models.migration.MigrationSource`nimport ireader.domain.models.migration.MigrationBook`n`$1"
    }
    
    # Fix getLibraryBooks method call
    $content = $content -replace 'getLibraryBooks', 'getLibraryBooksFlow'
    # Or if it's a different method:
    $content = $content -replace 'getLibraryBooksFlow', 'getFavoriteBooksFlow'
    
    # Fix Flow type inference
    $content = $content -replace '\.map\s*\{', '.map<List<Book>, List<MigrationBook>> {'
    $content = $content -replace '\.combine\s*\(', '.combine<List<MigrationBook>, Set<Long>, List<MigrationBook>>('
    
    # Fix MigrationBook references
    $content = $content -replace 'MigrationBook\(', 'ireader.domain.models.migration.MigrationBook('
    
    Set-Content $file $content -NoNewline
    Write-Host "   Updated MigrationScreenModel.kt" -ForegroundColor Green
}

Write-Host "`nMigration model fixes applied!" -ForegroundColor Green
Write-Host "Note: Using domain models from ireader.domain.models.migration" -ForegroundColor Yellow
