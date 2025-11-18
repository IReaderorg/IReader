# Fix mutableState.value usage to use updateState() instead
# This script converts Voyager-style state updates to IReaderStateScreenModel pattern

Write-Host "Fixing mutableState.value usage..." -ForegroundColor Cyan
Write-Host "==================================`n" -ForegroundColor Cyan

$fixCount = 0

$screenModelFiles = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreenModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatsScreenModel.kt"
)

foreach ($filePath in $screenModelFiles) {
    if (Test-Path $filePath) {
        Write-Host "Processing: $filePath" -ForegroundColor Yellow
        $content = Get-Content $filePath -Raw
        $originalContent = $content
        
        # Pattern 1: mutableState.value = mutableState.value.copy(...) -> updateState { it.copy(...) }
        $content = $content -replace 'mutableState\.value\s*=\s*mutableState\.value\.copy\(', 'updateState { it.copy('
        
        # Pattern 2: val x = mutableState.value.property -> val x = state.value.property
        $content = $content -replace '=\s*mutableState\.value\.', '= state.value.'
        
        # Pattern 3: mutableState.value.property in expressions -> state.value.property
        $content = $content -replace '([^=])\s*mutableState\.value\.', '$1 state.value.'
        
        # Pattern 4: Fix forEach with mutableState.value -> state.value
        $content = $content -replace 'mutableState\.value\.selectedDownloads\.forEach', 'state.value.selectedDownloads.forEach'
        
        if ($content -ne $originalContent) {
            Set-Content $filePath -Value $content -NoNewline
            Write-Host "  Fixed: $filePath" -ForegroundColor Green
            $fixCount++
        } else {
            Write-Host "  No changes needed" -ForegroundColor Gray
        }
    } else {
        Write-Host "  File not found: $filePath" -ForegroundColor Red
    }
}

Write-Host "`n==================================`n" -ForegroundColor Cyan
Write-Host "Total files fixed: $fixCount" -ForegroundColor Green

if ($fixCount -gt 0) {
    Write-Host "`nNote: Please review the changes to ensure correctness." -ForegroundColor Yellow
    Write-Host "Some complex patterns may need manual adjustment." -ForegroundColor Yellow
}
