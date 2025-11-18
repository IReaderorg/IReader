# Convert rememberScreenModel to getIViewModel pattern
# This script converts all Voyager ScreenModel usage to IReader ViewModel pattern

Write-Host "=== Converting to ViewModel Pattern ===" -ForegroundColor Cyan

$FixCount = 0

# Helper function to convert a file
function Convert-ToViewModel {
    param(
        [string]$FilePath,
        [string]$ViewModelClass,
        [string]$ParameterPattern
    )
    
    if (-not (Test-Path $FilePath)) {
        return $false
    }
    
    $content = Get-Content -Path $FilePath -Raw -Encoding UTF8
    $modified = $false
    
    # Remove Voyager imports
    if ($content -match 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel') {
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel\s*\n', ''
        $modified = $true
    }
    
    # Add Koin imports if not present
    if ($content -notmatch 'import org\.koin\.compose\.koinViewModel') {
        $content = $content -replace '(import org\.koin\.core\.parameter\.parametersOf)', "`$1`nimport org.koin.compose.koinViewModel"
        $modified = $true
    }
    
    # Convert rememberScreenModel to koinViewModel
    # Pattern 1: rememberScreenModel { ClassName(...) }
    $pattern1 = "rememberScreenModel\s*\{\s*$ViewModelClass\s*\([^)]*\)\s*\}"
    if ($content -match $pattern1) {
        $content = $content -replace $pattern1, "koinViewModel<$ViewModelClass>(parameters = { $ParameterPattern })"
        $modified = $true
    }
    
    # Pattern 2: rememberScreenModel(tag = ...) { ClassName(...) }
    $pattern2 = "rememberScreenModel\s*\([^)]*\)\s*\{\s*$ViewModelClass\s*\([^)]*\)\s*\}"
    if ($content -match $pattern2) {
        $content = $content -replace $pattern2, "koinViewModel<$ViewModelClass>(parameters = { $ParameterPattern })"
        $modified = $true
    }
    
    if ($modified) {
        Set-Content -Path $FilePath -Value $content -Encoding UTF8 -NoNewline
        return $true
    }
    
    return $false
}

# Fix 1: BookDetailScreenEnhanced.kt
Write-Host "`n[1] Converting BookDetailScreenEnhanced.kt..." -ForegroundColor Yellow
$enhancedPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenEnhanced.kt"
if (Test-Path $enhancedPath) {
    $content = Get-Content -Path $enhancedPath -Raw -Encoding UTF8
    
    # Remove Voyager and Koin getKoin imports
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel\s*\n', ''
    $content = $content -replace 'import org\.koin\.compose\.getKoin\s*\n', ''
    
    # Add koinViewModel import
    if ($content -notmatch 'import org\.koin\.compose\.koinViewModel') {
        $content = $content -replace '(import org\.koin\.core\.parameter\.parametersOf)', "`$1`nimport org.koin.compose.koinViewModel"
    }
    
    # Remove koin variable declaration
    $content = $content -replace 'val koin = getKoin\(\)\s*\n\s*', ''
    
    # Convert rememberScreenModel to koinViewModel
    $oldPattern = @'
rememberScreenModel { 
        BookDetailScreenModelNew(
            bookId = bookId,
            getBookUseCases = koin.get(),
            getChapterUseCase = koin.get(),
            insertUseCases = koin.get()
        )
    }
'@
    
    $newPattern = 'koinViewModel<BookDetailViewModel>(parameters = { parametersOf(BookDetailViewModel.Param(bookId)) })'
    
    $content = $content -replace [regex]::Escape($oldPattern), $newPattern
    
    # Also handle if it's on one line or different formatting
    $content = $content -replace 'rememberScreenModel\s*\{\s*BookDetailScreenModelNew\([^}]+\}\s*\}', $newPattern
    
    # Change the variable type if needed
    $content = $content -replace 'val screenModel = koinViewModel', 'val vm: BookDetailViewModel = koinViewModel'
    
    Set-Content -Path $enhancedPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted BookDetailScreenEnhanced.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 2: BookDetailScreenNew.kt
Write-Host "`n[2] Converting BookDetailScreenNew.kt..." -ForegroundColor Yellow
$newPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt"
if (Test-Path $newPath) {
    $content = Get-Content -Path $newPath -Raw -Encoding UTF8
    
    # Remove Voyager and Koin getKoin imports
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel\s*\n', ''
    $content = $content -replace 'import org\.koin\.compose\.getKoin\s*\n', ''
    
    # Add koinViewModel import
    if ($content -notmatch 'import org\.koin\.compose\.koinViewModel') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.koinViewModel`n"
    }
    
    # Convert rememberScreenModel to koinViewModel
    $content = $content -replace 'rememberScreenModel', 'koinViewModel<BookDetailViewModel>'
    
    Set-Content -Path $newPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted BookDetailScreenNew.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 3: BookDetailScreenRefactored.kt
Write-Host "`n[3] Converting BookDetailScreenRefactored.kt..." -ForegroundColor Yellow
$refactoredPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenRefactored.kt"
if (Test-Path $refactoredPath) {
    $content = Get-Content -Path $refactoredPath -Raw -Encoding UTF8
    
    # Remove Voyager imports
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel\s*\n', ''
    
    # Add koinViewModel import
    if ($content -notmatch 'import org\.koin\.compose\.koinViewModel') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.koinViewModel`n"
    }
    
    # Convert rememberScreenModel to koinViewModel
    $content = $content -replace 'rememberScreenModel\s*\([^)]*\)\s*\{', 'koinViewModel<BookDetailViewModel>(parameters = {'
    
    Set-Content -Path $refactoredPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted BookDetailScreenRefactored.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 4: DownloadScreens.kt
Write-Host "`n[4] Converting DownloadScreens.kt..." -ForegroundColor Yellow
$downloadPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreens.kt"
if (Test-Path $downloadPath) {
    $content = Get-Content -Path $downloadPath -Raw -Encoding UTF8
    
    # Remove Voyager and getKoin imports
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel\s*\n', ''
    $content = $content -replace 'import org\.koin\.compose\.getKoin\s*\n', ''
    
    # Add koinViewModel import
    if ($content -notmatch 'import org\.koin\.compose\.koinViewModel') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.koinViewModel`n"
    }
    
    # Remove koin variable
    $content = $content -replace 'val koin = getKoin\(\)\s*\n\s*', ''
    
    # Convert rememberScreenModel to koinViewModel
    $content = $content -replace 'rememberScreenModel', 'koinViewModel<DownloadScreenModel>'
    
    Set-Content -Path $downloadPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted DownloadScreens.kt" -ForegroundColor Green
    $FixCount++
}

# Fix 5: MigrationScreens.kt
Write-Host "`n[5] Converting MigrationScreens.kt..." -ForegroundColor Yellow
$migrationPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt"
if (Test-Path $migrationPath) {
    $content = Get-Content -Path $migrationPath -Raw -Encoding UTF8
    
    # Remove Voyager and getKoin imports
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.rememberScreenModel\s*\n', ''
    $content = $content -replace 'import org\.koin\.compose\.getKoin\s*\n', ''
    
    # Add koinViewModel import
    if ($content -notmatch 'import org\.koin\.compose\.koinViewModel') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.koinViewModel`n"
    }
    
    # Remove koin variable
    $content = $content -replace 'val koin = getKoin\(\)\s*\n\s*', ''
    
    # Convert rememberScreenModel to koinViewModel
    $content = $content -replace 'rememberScreenModel', 'koinViewModel<MigrationScreenModel>'
    
    Set-Content -Path $migrationPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Converted MigrationScreens.kt" -ForegroundColor Green
    $FixCount++
}

Write-Host "`n=== Conversion Summary ===" -ForegroundColor Cyan
Write-Host "Files converted: $FixCount" -ForegroundColor Green
Write-Host ""
Write-Host "Changes made:" -ForegroundColor Yellow
Write-Host "  - Removed Voyager rememberScreenModel imports" -ForegroundColor White
Write-Host "  - Added Koin koinViewModel imports" -ForegroundColor White
Write-Host "  - Converted rememberScreenModel to koinViewModel" -ForegroundColor White
Write-Host "  - Updated to use parametersOf for ViewModel parameters" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Compile: .\gradlew :presentation:compileReleaseKotlinAndroid" -ForegroundColor White
Write-Host "2. Verify ViewModel injection works correctly" -ForegroundColor White
