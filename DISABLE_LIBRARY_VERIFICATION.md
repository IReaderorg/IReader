# Disabling Library Verification (Development Only)

## Quick Fix for Development

If you're getting library verification errors during development, you can temporarily disable verification.

### Option 1: System Property (Recommended for Development)

Add this to your run configuration or gradle command:

```bash
# Gradle
./gradlew desktop:run -Dpiper.verify.libraries=false

# Or in build.gradle.kts
run {
    systemProperty("piper.verify.libraries", "false")
}
```

### Option 2: IntelliJ IDEA Run Configuration

1. Open Run → Edit Configurations
2. Select your Desktop run configuration
3. Add to VM options: `-Dpiper.verify.libraries=false`
4. Click OK and run

### Option 3: Update Checksums (Production Approach)

When you build new native libraries, update their checksums in:
`domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/LibraryVerifier.kt`

**Generate checksums:**

```powershell
# Windows
Get-FileHash -Algorithm SHA256 domain\src\desktopMain\resources\native\windows-x64\piper_jni.dll

# Linux/macOS
shasum -a 256 domain/src/desktopMain/resources/native/linux-x64/libpiper_jni.so
```

**Update the map:**

```kotlin
private val knownChecksums = mapOf(
    "piper_jni.dll" to "YOUR_ACTUAL_CHECKSUM_HERE",
    // ...
)
```

## Current Status

✅ **piper_jni.dll checksum has been updated** to:
```
BA2CE5E17DC4579F04445DDC824030F8237D02915DDA626C8E7BF9CAAF0128A1
```

The library should now load without verification errors!

## Security Note

⚠️ **Never disable verification in production!**

Library verification protects against:
- Corrupted libraries
- Tampered libraries
- Malicious code injection

Only disable for:
- Local development
- Testing new builds
- Debugging

## Troubleshooting

### Still getting verification errors?

1. **Check the DLL location:**
   ```
   domain/src/desktopMain/resources/native/windows-x64/piper_jni.dll
   ```

2. **Verify the checksum matches:**
   ```powershell
   Get-FileHash -Algorithm SHA256 domain\src\desktopMain\resources\native\windows-x64\piper_jni.dll
   ```

3. **Rebuild the application:**
   ```bash
   ./gradlew clean build
   ```

4. **Check for multiple copies:**
   ```powershell
   dir piper_jni.dll /s
   ```

### Error: "Checksum mismatch"

This means the DLL file has changed. Either:
- Update the checksum in `LibraryVerifier.kt`
- Or disable verification for development

### Error: "No valid signature"

On Windows, code signing is optional for development. Either:
- Disable signature verification: `-Dpiper.verify.signatures=false`
- Or sign the DLL with a code signing certificate

## Re-enabling Verification

Before releasing to production:

1. Remove `-Dpiper.verify.libraries=false`
2. Ensure all checksums are correct
3. Sign libraries with code signing certificates
4. Test on clean systems

## Questions?

See the full documentation in:
- `native/BUILD_PIPER_JNI.md` - Building the DLL
- `native/licensing/README.md` - License compliance
- `native/qa/README.md` - Quality assurance
