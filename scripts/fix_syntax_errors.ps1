# Fix Syntax Errors from Previous Fixes
# This script fixes syntax errors introduced by regex replacements

Write-Host "Fixing syntax errors..." -ForegroundColor Cyan

# Fix 1: Check for broken activity references
Write-Host "1. Checking for broken activity references..." -ForegroundColor Yellow
$files = Get-ChildItem -Recurse -Filter "*.kt" -Path "presentation/src/commonMain" | Where-Object {
    $content = Get-Content $_.FullName -Raw
    $content -match 'androidx\.// activity\.'
}

foreach ($file in $files) {
    Write-Host "   Fixing $($file.Name)..." -ForegroundColor Gray
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'androidx\.// activity\. // Android-specific, implement in androidMaincompose\.BackHandler', '// androidx.activity.compose.BackHandler // Android-specific'
    Set-Content $file.FullName $content -NoNewline
}

# Fix 2: Check for orphaned closing braces after BackHandler removal
Write-Host "2. Checking for orphaned braces..." -ForegroundColor Yellow
$files = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/core/ui/BookDetailScreenSpec.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ReaderScreenSpec.kt"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        # Remove orphaned closing braces after BackHandler comments
        $content = $content -replace '(// BackHandler removed[^\n]+\n)\s*\}\s*\n', '$1'
        Set-Content $file $content -NoNewline
    }
}

# Fix 3: Fix DynamicColors commented code
Write-Host "3. Fixing DynamicColors..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/theme/DynamicColors.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Replace the broken dynamic color functions with proper expect/actual pattern
    $content = $content -replace '// dynamicDarkColorScheme // Android-specific', 'null // dynamicDarkColorScheme - implement in androidMain'
    $content = $content -replace '// dynamicLightColorScheme // Android-specific', 'null // dynamicLightColorScheme - implement in androidMain'
    $content = $content -replace '// LocalContext // Android-specific', 'null // LocalContext - implement in androidMain'
    $content = $content -replace '// android\. // Android-specific', '// android - implement in androidMain'
    
    Set-Content $file $content -NoNewline
}

Write-Host "`nSyntax error fixes applied!" -ForegroundColor Green
