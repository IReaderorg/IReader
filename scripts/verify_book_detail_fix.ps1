# Verify BookDetailScreenNew.kt Fix

Write-Host "=== Verifying BookDetailScreenNew.kt Fix ===" -ForegroundColor Cyan

$filePath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt"

if (-not (Test-Path $filePath)) {
    Write-Host "ERROR: File not found" -ForegroundColor Red
    exit 1
}

$content = Get-Content -Path $filePath -Raw

$allPassed = $true

# Check correct imports
if ($content -match "import ireader\.presentation\.core\.ui\.getIViewModel") {
    Write-Host "OK: Correct getIViewModel import" -ForegroundColor Green
} else {
    Write-Host "FAIL: Missing correct getIViewModel import" -ForegroundColor Red
    $allPassed = $false
}

if ($content -match "import ireader\.presentation\.ui\.book\.viewmodel\.BookDetailViewModel") {
    Write-Host "OK: BookDetailViewModel import" -ForegroundColor Green
} else {
    Write-Host "FAIL: Missing BookDetailViewModel import" -ForegroundColor Red
    $allPassed = $false
}

if ($content -match "val vm: BookDetailViewModel = getIViewModel") {
    Write-Host "OK: Correct ViewModel initialization" -ForegroundColor Green
} else {
    Write-Host "FAIL: Incorrect ViewModel initialization" -ForegroundColor Red
    $allPassed = $false
}

if ($allPassed) {
    Write-Host "`nAll checks passed!" -ForegroundColor Green
} else {
    Write-Host "`nSome checks failed" -ForegroundColor Red
}
