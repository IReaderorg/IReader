# Fix Critical Compilation Errors
# This script fixes the most critical errors preventing compilation

Write-Host "Fixing critical compilation errors..." -ForegroundColor Cyan

# Fix 1: Remove androidx.activity.compose.BackHandler from commonMain (Android-specific)
Write-Host "`n1. Fixing BackHandler in BookDetailScreenSpec.kt..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/core/ui/BookDetailScreenSpec.kt"
$content = Get-Content $file -Raw
$content = $content -replace 'androidx\.activity\.compose\.BackHandler\(enabled = sheetState\.isVisible\) \{[^}]+\}', '// BackHandler removed - Android-specific, implement in androidMain if needed'
Set-Content $file $content -NoNewline

# Fix 2: Remove androidx.activity.compose.BackHandler from ReaderScreenSpec.kt
Write-Host "2. Fixing BackHandler in ReaderScreenSpec.kt..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ReaderScreenSpec.kt"
$content = Get-Content $file -Raw
$content = $content -replace 'androidx\.activity\.compose\.BackHandler\(enabled = drawerState\.isOpen\) \{[^}]+\}', '// BackHandler removed - Android-specific, implement in androidMain if needed'
Set-Content $file $content -NoNewline

# Fix 3: Fix ImageLoader placeholder return type
Write-Host "3. Fixing ImageLoader placeholder..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/imageloader/ImageLoader.kt"
$content = Get-Content $file -Raw
# The placeholder should return Image?, not ColorPainter
$content = $content -replace 'placeholder\s*\{\s*placeholderPainter\s*\}', 'placeholder { _ -> placeholderPainter.toImage() }'
Set-Content $file $content -NoNewline

# Fix 4: Fix BookDetailScreenNew error references
Write-Host "4. Fixing BookDetailScreenNew error references..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt"
$content = Get-Content $file -Raw
$content = $content -replace 'vm\.booksState\.error', 'vm.error'
$content = $content -replace 'message = vm\.error!!', 'message = vm.error?.message ?: "Unknown error"'
Set-Content $file $content -NoNewline

# Fix 5: Fix AccessibilityUtils ripple import and usage
Write-Host "5. Fixing AccessibilityUtils ripple..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/accessibility/AccessibilityUtils.kt"
$content = Get-Content $file -Raw

# Add import if not present
if ($content -notmatch 'import androidx\.compose\.material\.ripple\.ripple') {
    $content = $content -replace '(package ireader\.presentation\.ui\.component\.accessibility)', "`$1`n`nimport androidx.compose.material.ripple.ripple"
}

# Fix size modifiers - add .dp
$content = $content -replace '\.size\((\d+)\)', '.size($1.dp)'
$content = $content -replace '\.size\(minWidth = (\d+), minHeight = (\d+)\)', '.size(minWidth = $1.dp, minHeight = $1.dp)'

Set-Content $file $content -NoNewline

# Fix 6: Fix AccessibleBookListItem Text import
Write-Host "6. Fixing AccessibleBookListItem..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/component/enhanced/AccessibleBookListItem.kt"
$content = Get-Content $file -Raw

# Ensure Text is imported from material3
if ($content -notmatch 'import androidx\.compose\.material3\.Text') {
    $content = $content -replace '(package ireader\.presentation\.ui\.component\.enhanced)', "`$1`n`nimport androidx.compose.material3.Text"
}

Set-Content $file $content -NoNewline

# Fix 7: Fix StateViewModel stateIn usage
Write-Host "7. Fixing StateViewModel stateIn..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/core/viewmodel/StateViewModel.kt"
$content = Get-Content $file -Raw

# Fix the stateIn call - it should use asStateFlow() for MutableStateFlow
$content = $content -replace 'val state: StateFlow<S> = mutableState\.stateIn\(scope\)', 'val state: StateFlow<S> = mutableState.asStateFlow()'

Set-Content $file $content -NoNewline

Write-Host "`nCritical fixes applied!" -ForegroundColor Green
Write-Host "Run: .\gradlew :presentation:compileKotlinDesktop" -ForegroundColor Cyan
