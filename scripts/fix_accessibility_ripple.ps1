# Fix AccessibilityUtils Ripple and Size Issues

Write-Host "Fixing AccessibilityUtils..." -ForegroundColor Cyan

$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/accessibility/AccessibilityUtils.kt"

if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add ripple import if missing
    if ($content -notmatch 'import androidx\.compose\.material\.ripple') {
        Write-Host "1. Adding ripple import..." -ForegroundColor Yellow
        $content = $content -replace '(package ireader\.presentation\.ui\.component\.accessibility\n)', "`$1`nimport androidx.compose.material.ripple.ripple`n"
    }
    
    # Add Dp import if missing
    if ($content -notmatch 'import androidx\.compose\.ui\.unit\.dp') {
        Write-Host "2. Adding dp import..." -ForegroundColor Yellow
        $content = $content -replace '(package ireader\.presentation\.ui\.component\.accessibility\n)', "`$1`nimport androidx.compose.ui.unit.dp`n"
    }
    
    # Fix size modifiers - ensure they have .dp
    Write-Host "3. Fixing size modifiers..." -ForegroundColor Yellow
    # Pattern: .size(number) -> .size(number.dp)
    $content = $content -replace '\.size\((\d+)\)(?!\.dp)', '.size($1.dp)'
    # Pattern: .size(minWidth = number, minHeight = number) -> with .dp
    $content = $content -replace '\.size\(minWidth\s*=\s*(\w+),\s*minHeight\s*=\s*(\w+)\)(?!\.dp)', '.size(minWidth = $1, minHeight = $2)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed AccessibilityUtils" -ForegroundColor Green
} else {
    Write-Host "   File not found: $file" -ForegroundColor Red
}

Write-Host "`nAccessibilityUtils fixes applied!" -ForegroundColor Green
