if ($(Split-Path -Path (Get-Location) -Leaf) -eq "scripts" ) {
    Set-Location ..
}

Write-Output "Writing ci gradle.properties"
if (!(Test-Path -Path ".gradle")) {
    New-Item -ItemType Directory -Force -Path ".gradle" -ErrorAction SilentlyContinue
}
Copy-Item ".github/runner-files/ci-gradle.properties" ".gradle/gradle.properties" -Force