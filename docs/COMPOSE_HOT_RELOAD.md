# Compose Hot Reload Setup Guide

Compose Hot Reload is now configured for the IReader desktop application. This allows you to see UI changes instantly without restarting the app.

## Prerequisites

✅ All requirements are met:
- Kotlin 2.3.10 (requires 2.1.20+)
- Compose Multiplatform 1.10.1 (requires 1.8.2+)
- Java 21 (requires Java 21 or earlier)
- Desktop target configured

## Running with Hot Reload

### Option 1: From Command Line (Explicit Mode)

```bash
# Start the desktop app with hot reload
./gradlew :desktop:hotRun

# Make changes to your Compose UI code
# Save the file (Ctrl+S / Cmd+S)
# Changes apply automatically
```

### Option 2: Auto Mode

```bash
# Automatically reload on file changes
./gradlew :desktop:hotRun --autoReload

# Or use the short form
./gradlew :desktop:hotRun --auto
```

### Option 3: From IntelliJ IDEA / Android Studio

1. Open any Compose file (e.g., `QuotesScreen.kt`)
2. Look for the green ▶️ icon in the gutter next to the `main` function
3. Click it and select **"Run 'shared [jvm]' with Compose Hot Reload"**
4. Make UI changes and save (Ctrl+S / Cmd+S)
5. Changes apply instantly!

## What Can Be Hot Reloaded?

✅ **Works great:**
- Composable functions
- UI layouts and styling
- Colors, padding, spacing
- Text content
- Animations and transitions
- Conditional UI logic

❌ **Requires restart:**
- ViewModel logic
- Repository implementations
- Dependency injection changes
- Non-Compose code
- Data classes used in state

## Keyboard Shortcuts

When running with hot reload:
- **Ctrl+S / Cmd+S**: Save and trigger reload (Explicit mode)
- **Ctrl+C**: Stop the hot reload session

## IDE Configuration

You can customize hot reload behavior in:
**Settings → Tools → Compose Hot Reload**

Options:
- Trigger mode (Explicit vs Auto)
- Keyboard shortcuts
- Reload notifications

## Troubleshooting

### "JetBrains Runtime not found"
The Foojay resolver will automatically download the JBR. If it fails:
```bash
# Enable experimental auto-provisioning
./gradlew :desktop:hotRun -Pcompose.reload.jbr.autoProvisioningEnabled=true
```

### "Hot reload not working"
1. Ensure you're editing Compose UI code (not ViewModels)
2. Save the file after making changes
3. Check the console for reload confirmation
4. Try restarting the hot reload session

### "Changes not visible"
Some changes require a full restart:
- State management changes
- ViewModel modifications
- Dependency injection updates

## Performance Tips

1. **Use Auto Mode for rapid iteration**: `--autoReload` watches files continuously
2. **Keep ViewModels separate**: Hot reload works best with pure Compose UI
3. **Test on desktop first**: Iterate quickly, then test on Android/iOS
4. **Use preview functions**: Combine with `@Preview` for even faster iteration

## Example Workflow

```kotlin
// 1. Start hot reload
// ./gradlew :desktop:hotRun --autoReload

// 2. Edit QuotesScreen.kt
@Composable
fun QuotesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)  // Change this to 24.dp
    ) {
        Text("My Quotes")  // Change text
    }
}

// 3. Save file (Ctrl+S)
// 4. See changes instantly in running app!
```

## Benefits

- **10x faster UI iteration**: No more waiting for rebuilds
- **Maintain app state**: No need to navigate back to your screen
- **Flow state**: Stay focused on design without interruptions
- **Rapid prototyping**: Try multiple designs in seconds

## Limitations

- Desktop target only (Android/iOS not supported yet)
- UI code only (business logic requires restart)
- Java 21 or earlier required (Java 24+ not compatible)

## Resources

- [Official Documentation](https://kotlinlang.org/docs/multiplatform/compose-hot-reload.html)
- [GitHub Repository](https://github.com/JetBrains/compose-hot-reload)
- [JetBrains Blog Post](https://blog.jetbrains.com/kotlin/2026/01/the-journey-to-compose-hot-reload-1-0-0/)

---

**Happy hot reloading! 🔥**
