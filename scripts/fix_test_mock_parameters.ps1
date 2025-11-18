# Script to fix test mock parameter issues
# This script fixes "No value passed for parameter 'block'" errors in test files

Write-Host "Fixing test mock parameter issues..." -ForegroundColor Cyan

$testFiles = @(
    "data/src/commonTest/kotlin/ireader/data/repository/consolidated/BookRepositoryTest.kt",
    "data/src/commonTest/kotlin/ireader/data/repository/consolidated/ChapterRepositoryTest.kt"
)

$replacements = @{
    # Fix handler.await calls - add second parameter
    'handler\.await<Unit>\(any\(\)\)' = 'handler.await<Unit>(any(), any())'
    'handler\.await<Unit>\(inTransaction = true, any\(\)\)' = 'handler.await<Unit>(any(), any())'
    
    # Fix handler.awaitList calls - add second parameter
    'handler\.awaitList<\w+>\(any\(\)\)' = 'handler.awaitList<$1>(any(), any())'
    
    # Fix handler.awaitOneOrNull calls - add second parameter
    'handler\.awaitOneOrNull<\w+>\(any\(\)\)' = 'handler.awaitOneOrNull<$1>(any(), any())'
}

foreach ($file in $testFiles) {
    if (Test-Path $file) {
        Write-Host "Processing: $file" -ForegroundColor Yellow
        $content = Get-Content $file -Raw
        
        # Apply replacements
        $modified = $false
        foreach ($pattern in $replacements.Keys) {
            $replacement = $replacements[$pattern]
            if ($content -match $pattern) {
                $content = $content -replace $pattern, $replacement
                $modified = $true
            }
        }
        
        if ($modified) {
            Set-Content -Path $file -Value $content -NoNewline
            Write-Host "  ✓ Fixed" -ForegroundColor Green
        } else {
            Write-Host "  - No changes needed" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ✗ File not found: $file" -ForegroundColor Red
    }
}

Write-Host "`nTest mock parameter fixes completed!" -ForegroundColor Green
Write-Host "Run './gradlew :data:compileTestKotlinDesktop' to verify the fixes." -ForegroundColor Cyan
