# Script to fix common compilation errors in the IReader project
# This script addresses multiple error patterns identified in the codebase

Write-Host "Starting comprehensive compilation error fixes..." -ForegroundColor Cyan

$fixCount = 0
$errorPatterns = @()

# Pattern 1: Fix StateScreenModel imports and usage
Write-Host "`n1. Fixing StateScreenModel references..." -ForegroundColor Yellow
$files = Get-ChildItem -Path "presentation" -Filter "*.kt" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Replace Voyager StateScreenModel imports with IReaderStateScreenModel
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.StateScreenModel', 'import ireader.presentation.core.viewmodel.IReaderStateScreenModel'
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.screenModelScope', '// screenModelScope is provided by IReaderStateScreenModel'
    $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.mutableState', '// mutableState not needed - use updateState()'
    
    # Replace StateScreenModel class inheritance
    $content = $content -replace ': StateScreenModel<([^>]+)>\(([^)]+)\)', ': IReaderStateScreenModel<$1>($2)'
    
    # Replace mutableState calls with updateState
    $content = $content -replace 'mutableState\s*\{\s*([^}]+)\s*\}', 'updateState { $1 }'
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "  Fixed: $($file.Name)" -ForegroundColor Green
        $fixCount++
    }
}

# Pattern 2: Fix asStateFlow() calls on Preferences
Write-Host "`n2. Fixing asStateFlow() on Preference objects..." -ForegroundColor Yellow
$files = Get-ChildItem -Path "presentation" -Filter "*ViewModel.kt" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Fix .asStateFlow() without parameters to .stateIn(scope)
    $content = $content -replace '\.asStateFlow\(\)', '.stateIn(scope)'
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "  Fixed: $($file.Name)" -ForegroundColor Green
        $fixCount++
    }
}

# Pattern 3: Fix onPopBackStack parameter name
Write-Host "`n3. Fixing onPopBackStack parameter names..." -ForegroundColor Yellow
$files = Get-ChildItem -Path "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings" -Filter "*.kt" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Replace onPopBackStack with popBackStack in function calls
    $content = $content -replace 'onPopBackStack\s*=\s*', 'popBackStack = '
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "  Fixed: $($file.Name)" -ForegroundColor Green
        $fixCount++
    }
}

# Pattern 4: Fix .map() calls on Preference objects
Write-Host "`n4. Fixing .map() on Preference objects..." -ForegroundColor Yellow
$files = Get-ChildItem -Path "presentation" -Filter "*ViewModel.kt" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Fix .map { it } pattern on preferences - should use .changes().map { it }
    $content = $content -replace '(preferenceStore\.[a-zA-Z]+\([^)]+\))\.map\s*\{', '$1.changes().map {'
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "  Fixed: $($file.Name)" -ForegroundColor Green
        $fixCount++
    }
}

Write-Host "`n=== Fix Summary ===" -ForegroundColor Cyan
Write-Host "Total files fixed: $fixCount" -ForegroundColor Yellow

if ($fixCount -eq 0) {
    Write-Host "No common errors found!" -ForegroundColor Green
} else {
    Write-Host "`nPlease review the changes and test compilation." -ForegroundColor Yellow
}
