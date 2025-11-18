# Fix remaining screenModel references to vm

Write-Host "=== Fixing screenModel References ===" -ForegroundColor Cyan

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
        
        # Replace screenModel with vm
        $content = $content -replace '\bscreenModel\.', 'vm.'
        $content = $content -replace '\bscreenModel\b(?!\.)', 'vm'
        
        # Fix the state collection line if it exists
        $content = $content -replace 'val state by vm\.state\.collectAsState\(\)', 'val state by vm.state.collectAsState()'
        
        if ($content -ne $originalContent) {
            Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
            $fileName = Split-Path $file -Leaf
            Write-Host "  Fixed $fileName" -ForegroundColor Green
            $FixCount++
        }
    }
}

Write-Host "`n=== Fix Summary ===" -ForegroundColor Cyan
Write-Host "Files fixed: $FixCount" -ForegroundColor Green
