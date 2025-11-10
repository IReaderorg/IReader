# Quick Build Reference

## Build Commands

### Windows
```powershell
cmake -S native -B native\build\windows -G "Visual Studio 17 2022" -A x64
cmake --build native\build\windows --config Release --parallel
```

### macOS
```bash
cd native && ./scripts/build_macos.sh release
```

### Linux
```bash
cd native && ./scripts/build_linux.sh release
```

## Verification

### Check All Libraries
```powershell
.\native\scripts\verify_build.ps1
```

### Generate Checksums
```powershell
.\native\scripts\generate_checksums.ps1
```

## CI/CD

### Trigger Build
Push to `main`, `develop`, or `feature/piper-jni-*` branches

### Manual Trigger
Go to Actions → Build Native Libraries → Run workflow

### Download Artifacts
Actions → Latest workflow run → Artifacts section

## Current Status

- ✅ Windows x64: Built (67.5 KB)
- ⏳ macOS x64: CI/CD ready
- ⏳ macOS ARM64: CI/CD ready
- ⏳ Linux x64: CI/CD ready

## Quick Links

- [Full Build Guide](BUILD_GUIDE.md)
- [Build Status](BUILD_STATUS.md)
- [Task 4 Summary](TASK_4_SUMMARY.md)
- [CI/CD Workflow](../.github/workflows/build-native-libs.yml)
