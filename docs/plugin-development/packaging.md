# Plugin Packaging and Distribution

Learn how to package and distribute your IReader plugin.

## Plugin Package Format

IReader plugins use the `.iplugin` format, which is a ZIP archive containing:

```
MyPlugin.iplugin (ZIP archive)
├── plugin.json          # Manifest file
├── classes/             # Compiled Kotlin classes
│   └── *.class
├── resources/           # Plugin resources
│   ├── icon.png
│   └── screenshots/
└── libs/                # Dependencies (optional)
    └── *.jar
```

## Building Your Plugin

### Using Gradle

Add the IReader plugin Gradle plugin to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("multiplatform") version "1.9.0"
    id("io.github.ireader.plugin") version "1.0.0"
}

kotlin {
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.ireader:plugin-api:1.0.0")
            }
        }
    }
}

ireaderPlugin {
    pluginName = "MyPlugin"
    outputDir = file("build/plugins")
}
```

### Build Command

```bash
./gradlew packagePlugin
```

This creates `MyPlugin.iplugin` in `build/plugins/`.

## Manual Packaging

If not using Gradle, you can package manually:

### 1. Compile Your Code

```bash
kotlinc -classpath plugin-api.jar MyPlugin.kt -d classes/
```

### 2. Create Directory Structure

```
MyPlugin/
├── plugin.json
├── classes/
│   └── MyPlugin.class
└── resources/
    └── icon.png
```

### 3. Create ZIP Archive

```bash
cd MyPlugin
zip -r ../MyPlugin.iplugin .
```

## Manifest Requirements

Your `plugin.json` must be valid and complete:

```json
{
  "id": "com.example.myplugin",
  "name": "My Plugin",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "Plugin description",
  "author": {
    "name": "Your Name",
    "email": "you@example.com"
  },
  "type": "THEME",
  "permissions": [],
  "minIReaderVersion": "1.0.0",
  "platforms": ["ANDROID", "IOS", "DESKTOP"]
}
```

## Validation

Before distribution, validate your plugin:

```bash
./gradlew validatePlugin --plugin=MyPlugin
```

This checks:
- Manifest format and completeness
- Required files present
- Class loading
- Permission declarations
- Version compatibility
- Platform compatibility

## Code Signing (Optional)

For enhanced security, sign your plugin:

```bash
./gradlew signPlugin --plugin=MyPlugin --keystore=my-keystore.jks
```

Signed plugins show a verified badge in the marketplace.

## Testing Your Package

### Local Installation

1. Copy `.iplugin` file to IReader's plugins directory:
   - Android: `/sdcard/IReader/plugins/`
   - iOS: App's documents directory
   - Desktop: `~/.ireader/plugins/`

2. Restart IReader

3. Enable plugin in Settings → Plugins

### Test Checklist

- [ ] Plugin loads without errors
- [ ] All features work as expected
- [ ] No crashes or freezes
- [ ] Permissions work correctly
- [ ] UI integrates properly
- [ ] Performance is acceptable
- [ ] Works on all target platforms

## Distribution Channels

### 1. IReader Plugin Marketplace (Recommended)

Submit to the official marketplace:

1. Create developer account at https://plugins.ireader.app
2. Upload `.iplugin` file
3. Fill in marketplace listing details
4. Submit for review
5. Once approved, plugin is available to all users

**Benefits:**
- Automatic updates
- Built-in payment processing
- User reviews and ratings
- Analytics dashboard
- Official verification

### 2. Direct Distribution

Share `.iplugin` file directly:

- Host on your website
- Share via GitHub releases
- Distribute through other channels

**Note:** Users must manually install and update.

### 3. GitHub Releases

Example workflow:

```yaml
name: Release Plugin

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      
      - name: Build plugin
        run: ./gradlew packagePlugin
      
      - name: Create Release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          draft: false
          prerelease: false
      
      - name: Upload Plugin
        uses: actions/upload-release-asset@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          upload_url: ${{ steps.create_release.outputs.upload_url }}
          asset_path: ./build/plugins/MyPlugin.iplugin
          asset_name: MyPlugin.iplugin
          asset_content_type: application/zip
```

## Versioning

### Version Numbers

Follow semantic versioning:

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

### Version Code

Increment `versionCode` with each release:

```json
{
  "version": "1.2.3",
  "versionCode": 5
}
```

Version codes must always increase.

## Updates

### Automatic Updates

If distributed through marketplace:

1. Upload new version
2. Users receive update notification
3. Update installs automatically (if enabled)

### Manual Updates

For direct distribution:

1. Release new `.iplugin` file
2. Users download and install manually
3. Old version is replaced

### Update Manifest

Include update information:

```json
{
  "updateUrl": "https://example.com/plugins/myplugin/latest.json",
  "changelog": "https://example.com/plugins/myplugin/changelog.md"
}
```

## Size Optimization

### Minimize Package Size

1. **Remove unused dependencies**
   ```kotlin
   dependencies {
       implementation("lib:needed:1.0")
       // Don't include unused libraries
   }
   ```

2. **Compress resources**
   - Optimize images (use WebP)
   - Minify JSON files
   - Remove debug symbols

3. **Use ProGuard/R8**
   ```kotlin
   ireaderPlugin {
       minify = true
       proguardFiles("proguard-rules.pro")
   }
   ```

4. **Exclude platform-specific code**
   ```kotlin
   kotlin {
       targets {
           jvm {
               // Only include what's needed
           }
       }
   }
   ```

### Target Size Guidelines

- Theme plugins: < 1 MB
- Translation plugins: < 5 MB
- TTS plugins: < 10 MB (without voice data)
- Feature plugins: < 5 MB

## Security Considerations

### 1. Don't Include Secrets

```kotlin
// Bad: Hardcoded API key
private const val API_KEY = "sk_live_abc123"

// Good: User-provided API key
private val apiKey: String
    get() = context.preferences.getString("api_key", "")
```

### 2. Validate All Inputs

```kotlin
fun processUserInput(input: String) {
    require(input.length <= MAX_LENGTH) { "Input too long" }
    require(input.matches(VALID_PATTERN)) { "Invalid format" }
    // Process input
}
```

### 3. Use HTTPS

```kotlin
private const val API_URL = "https://api.example.com" // Not http://
```

### 4. Minimize Permissions

Only request permissions you actually need:

```json
{
  "permissions": ["NETWORK"] // Don't request STORAGE if not needed
}
```

## Troubleshooting

### Plugin Won't Load

1. Check manifest format: `./gradlew validatePlugin`
2. Verify all required files are present
3. Check IReader version compatibility
4. Review error logs

### Plugin Crashes

1. Test with debug build
2. Check error logs
3. Verify all dependencies are included
4. Test on multiple devices/platforms

### Large Package Size

1. Remove unused dependencies
2. Optimize resources
3. Enable minification
4. Split platform-specific code

## Best Practices

1. **Test before releasing**: Always test on real devices
2. **Version properly**: Follow semantic versioning
3. **Document changes**: Maintain a changelog
4. **Optimize size**: Keep packages small
5. **Sign your plugin**: Use code signing for trust
6. **Provide updates**: Fix bugs and add features
7. **Support users**: Respond to issues and feedback

## Resources

- [Plugin Validator Tool](https://plugins.ireader.app/validator)
- [Developer Portal](https://plugins.ireader.app/developer)
- [Example Plugins](../examples/)
- [Community Forum](https://forum.ireader.app/plugins)
