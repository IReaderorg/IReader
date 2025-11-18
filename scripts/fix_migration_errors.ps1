# Fix Migration Screen Errors
# This script fixes migration-related compilation errors

Write-Host "Fixing migration screen errors..." -ForegroundColor Cyan

# Fix 1: MigrationScreenModel - getFavoritesAsFlow and Flow type inference
Write-Host "1. Fixing MigrationScreenModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix getFavoritesAsFlow to getLibraryBooks or similar
    $content = $content -replace 'getFavoritesAsFlow', 'getLibraryBooks'
    
    # Fix Flow type inference - add explicit types
    $content = $content -replace '\.map\s*\{', '.map<List<Book>, List<MigrationBook>> {'
    $content = $content -replace '\.combine\s*\(', '.combine<List<MigrationBook>, Set<Long>, List<MigrationBook>>('
    $content = $content -replace '\.distinctUntilChanged\s*\(\)', '.distinctUntilChanged<List<MigrationBook>>()'
    
    # Add necessary imports
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.map') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.map`nimport kotlinx.coroutines.flow.combine`nimport kotlinx.coroutines.flow.distinctUntilChanged"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix 2: MigrationScreens - MigrationFlags and related references
Write-Host "2. Fixing MigrationScreens..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add MigrationFlags enum if not present
    if ($content -notmatch 'enum class MigrationFlags') {
        $migrationFlagsEnum = @"

enum class MigrationFlags(val value: Int) {
    CHAPTERS(1 shl 0),
    CATEGORIES(1 shl 1),
    BOOKMARKS(1 shl 2),
    CUSTOM_COVER(1 shl 3),
    READING_PROGRESS(1 shl 4);
    
    companion object {
        fun hasFlag(flags: Int, flag: MigrationFlags): Boolean {
            return (flags and flag.value) != 0
        }
        
        fun setFlag(flags: Int, flag: MigrationFlags, enabled: Boolean): Int {
            return if (enabled) {
                flags or flag.value
            } else {
                flags and flag.value.inv()
            }
        }
    }
}

"@
        $content = $content -replace '(package ireader\.presentation\.ui\.migration)', "`$1`n$migrationFlagsEnum"
    }
    
    # Fix MigrationFlags references
    $content = $content -replace 'MigrationFlags\.', 'MigrationFlags.'
    
    # Fix property references (chapters, bookmarks, etc.)
    $content = $content -replace '\.chapters\b', '.migrateChapters'
    $content = $content -replace '\.bookmarks\b', '.migrateBookmarks'
    $content = $content -replace '\.categories\b', '.migrateCategories'
    $content = $content -replace '\.customCover\b', '.migrateCustomCover'
    $content = $content -replace '\.readingProgress\b', '.migrateReadingProgress'
    
    # Fix copy function references
    $content = $content -replace '\.copy\(chapters', '.copy(migrateChapters'
    $content = $content -replace '\.copy\(bookmarks', '.copy(migrateBookmarks'
    $content = $content -replace '\.copy\(categories', '.copy(migrateCategories'
    $content = $content -replace '\.copy\(customCover', '.copy(migrateCustomCover'
    $content = $content -replace '\.copy\(readingProgress', '.copy(migrateReadingProgress'
    
    # Add MigrationSource data class if not present
    if ($content -notmatch 'data class MigrationSource') {
        $migrationSourceClass = @"

data class MigrationSource(
    val id: Long,
    val sourceName: String,
    val priority: Int = 0
)

"@
        $content = $content -replace '(enum class MigrationFlags.*?\n\})', "`$1`n$migrationSourceClass"
    }
    
    # Fix MigrationSource references
    $content = $content -replace 'MigrationSource\.', 'MigrationSource.'
    
    # Fix sourceName and priority references
    $content = $content -replace '\.sourceName\b', '.sourceName'
    $content = $content -replace '\.priority\b', '.priority'
    
    # Fix when expression type mismatch
    $content = $content -replace 'if \(wideNavigationRail\)', 'if (wideNavigationRail == WideNavigationRailValue.Enabled)'
    $content = $content -replace '\.isEnabled', ''
    
    # Fix @Composable invocation
    $content = $content -replace '(@Composable\s+fun\s+\w+\([^)]*\)\s*\{[^}]*)(Text\()', '$1val text = Text; text()'
    
    Set-Content $file $content -NoNewline
}

Write-Host "`nMigration screen fixes applied!" -ForegroundColor Green
