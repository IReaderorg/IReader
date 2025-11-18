# Fix BookDetailScreen files - Koin and Voyager integration issues

Write-Host "=== Fixing BookDetailScreen Files ===" -ForegroundColor Cyan

Write-Host "`n[1] Fixing BookDetailScreenEnhanced.kt..." -ForegroundColor Yellow
$enhancedPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenEnhanced.kt"
if (Test-Path $enhancedPath) {
    $content = Get-Content -Path $enhancedPath -Raw -Encoding UTF8
    
    # Add missing imports
    if ($content -notmatch 'import org\.koin\.compose\.koinInject') {
        $content = $content -replace '(import org\.koin\.core\.parameter\.parametersOf)', "`$1`nimport org.koin.compose.koinInject`nimport org.koin.compose.getKoin"
    }
    
    # Fix the screenModel initialization
    $oldPattern = @'
    val screenModel = rememberScreenModel { 
        BookDetailScreenModelNew(
            bookId = bookId,
            getBookUseCases = get(),
            getChapterUseCase = get(),
            insertUseCases = get()
        )
    }
'@
    
    $newPattern = @'
    val koin = getKoin()
    val screenModel = rememberScreenModel { 
        BookDetailScreenModelNew(
            bookId = bookId,
            getBookUseCases = koin.get(),
            getChapterUseCase = koin.get(),
            insertUseCases = koin.get()
        )
    }
'@
    
    $content = $content -replace [regex]::Escape($oldPattern), $newPattern
    
    Set-Content -Path $enhancedPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed BookDetailScreenEnhanced.kt" -ForegroundColor Green
}

Write-Host "`n[2] Fixing BookDetailScreenNew.kt..." -ForegroundColor Yellow
$newPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt"
if (Test-Path $newPath) {
    $content = Get-Content -Path $newPath -Raw -Encoding UTF8
    
    # Add missing imports
    if ($content -notmatch 'import org\.koin\.compose\.koinInject') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.koinInject`nimport org.koin.compose.getKoin`n"
    }
    
    # Fix koin reference
    $content = $content -replace 'val\s+koin\s*=\s*koin\(\)', 'val koin = getKoin()'
    
    # Fix getScreenModel
    $content = $content -replace 'getScreenModel', 'rememberScreenModel'
    
    # Fix dp references
    $content = $content -replace '(\d+)(?!\.dp)', '$1.dp'
    
    Set-Content -Path $newPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed BookDetailScreenNew.kt" -ForegroundColor Green
}

Write-Host "`n[3] Fixing BookDetailScreenRefactored.kt..." -ForegroundColor Yellow
$refactoredPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenRefactored.kt"
if (Test-Path $refactoredPath) {
    $content = Get-Content -Path $refactoredPath -Raw -Encoding UTF8
    
    # Add missing imports
    if ($content -notmatch 'import org\.koin\.compose\.koinInject') {
        $content = $content -replace '(package .*\n)', "`$1`nimport org.koin.compose.koinInject`nimport org.koin.compose.getKoin`nimport cafe.adriel.voyager.core.model.rememberScreenModel`n"
    }
    
    # Fix rememberScreenModel
    $content = $content -replace 'rememberScreenModel\s*\{', 'rememberScreenModel(tag = bookId.toString()) {'
    
    Set-Content -Path $refactoredPath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Fixed BookDetailScreenRefactored.kt" -ForegroundColor Green
}

Write-Host "`n=== BookDetailScreen Fixes Complete ===" -ForegroundColor Cyan
Write-Host "All BookDetailScreen files have been updated" -ForegroundColor Green
