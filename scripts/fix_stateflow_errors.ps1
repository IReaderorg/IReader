# Script to fix common StateFlow conversion errors in Kotlin files
# This script identifies and fixes .asStateFlow() calls that should be .stateIn(scope)

Write-Host "Fixing StateFlow conversion errors..." -ForegroundColor Cyan

$fixCount = 0
$files = Get-ChildItem -Path "presentation" -Filter "*.kt" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Fix pattern: .asStateFlow() -> .stateIn(scope)
    # This pattern is for Preference<T> objects that need a scope parameter
    $content = $content -replace '\.asStateFlow\(\)', '.stateIn(scope)'
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "Fixed: $($file.FullName)" -ForegroundColor Green
        $fixCount++
    }
}

Write-Host "`nTotal files fixed: $fixCount" -ForegroundColor Yellow

if ($fixCount -eq 0) {
    Write-Host "No StateFlow errors found!" -ForegroundColor Green
}
