# Fix String Resource Imports
# Add missing string resource imports to screens

Write-Host "Fixing string resource imports..." -ForegroundColor Cyan

$files = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreenEnhanced.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/GlobalSearchScreenEnhanced.kt"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Processing $($file.Split('\')[-1])..." -ForegroundColor Yellow
        $content = Get-Content $file -Raw
        
        # Check if string resources import is missing
        if ($content -notmatch 'import ireader\.i18n\.resources\.\*') {
            # Find the package declaration
            if ($content -match '(package [^\n]+\n)') {
                # Add import after package and existing imports
                $content = $content -replace '(package [^\n]+\n)(import)', "`$1`nimport ireader.i18n.resources.*`n`$2"
            }
            
            Set-Content $file $content -NoNewline
            Write-Host "   Added string resources import" -ForegroundColor Green
        } else {
            Write-Host "   Already has string resources import" -ForegroundColor Gray
        }
    }
}

Write-Host "`nString resource imports fixed!" -ForegroundColor Green
