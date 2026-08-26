if ($(Split-Path -Path (Get-Location) -Leaf) -eq "scripts" ) {
    Set-Location ..
}

Write-Output "Writing ci gradle.properties"
$gradleUserHome = Join-Path $env:USERPROFILE ".gradle"
if (!(Test-Path -Path $gradleUserHome)) {
    New-Item -ItemType Directory -Force -Path $gradleUserHome -ErrorAction SilentlyContinue
}
Copy-Item ".github/runner-files/ci-gradle.properties" (Join-Path $gradleUserHome "gradle.properties") -Force
Get-Content ".github/runner-files/ci-gradle.properties" | Add-Content "gradle.properties"