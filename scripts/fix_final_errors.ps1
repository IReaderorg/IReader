# Fix Final Compilation Errors
# This script fixes the remaining compilation errors in the presentation module

Write-Host "Fixing final compilation errors..." -ForegroundColor Green

# Fix ExploreScreenEnhanced LocalizeHelper reference
$exploreFile = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreenEnhanced.kt"
if (Test-Path $exploreFile) {
    Write-Host "Fixing ExploreScreenEnhanced LocalizeHelper..." -ForegroundColor Yellow
    $content = Get-Content $exploreFile -Raw
    
    # Remove localizeHelper parameter from function signature
    $content = $content -replace 'localizeHelper: ireader\.presentation\.ui\.core\.theme\.LocalizeHelper,\s*', ''
    
    Set-Content $exploreFile $content -NoNewline
    Write-Host "Fixed ExploreScreenEnhanced" -ForegroundColor Green
}

Write-Host "`nAll fixes applied!" -ForegroundColor Green
Write-Host "Run './gradlew :presentation:compileKotlinDesktop' to verify" -ForegroundColor Cyan
