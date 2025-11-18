# Convert koinViewModel to getIViewModel pattern
# This matches the pattern used in BookDetailScreenSpec.kt

Write-Host "=== Converting to getIViewModel Pattern ===" -ForegroundColor Cyan

$files = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenEnhanced.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenRefactored.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreens.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt"
)

$FixCount = 0

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content -Path $file -Raw -Encoding UTF8
        $originalContent = $content
        
        # Remove koinViewModel import
        $content = $content -replace 'import org\.koin\.compose\.koinViewModel\s*\n', ''
        
        # Replace koinViewModel with getIViewModel
        $content = $content -replace 'koinViewModel<([^>]+)>\(parameters = \{', 'getIViewModel(parameters = {'
        
        # Also handle simpler patterns
        $content = $content -replace 'koinViewModel<([^>]+)>\(\)', 'getIViewModel()'
        
        if ($content -ne $originalContent) {
            Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
            $fileName = Split-Path $file -Leaf
            Write-Host "  Converted $fileName" -ForegroundColor Green
            $FixCount++
        }
    }
}

Write-Host "`n=== Conversion Summary ===" -ForegroundColor Cyan
Write-Host "Files converted: $FixCount" -ForegroundColor Green
Write-Host ""
Write-Host "Pattern used:" -ForegroundColor Yellow
Write-Host "  val vm: ViewModelClass = getIViewModel(parameters = { parametersOf(...) })" -ForegroundColor White
Write-Host ""
Write-Host "This matches the pattern in BookDetailScreenSpec.kt" -ForegroundColor Green
