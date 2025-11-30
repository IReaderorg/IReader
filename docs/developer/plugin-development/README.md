# IReader Plugin Development Guide

Welcome to the IReader Plugin Development Guide! This documentation will help you create plugins that extend IReader's functionality.

## Table of Contents

1. [Getting Started](getting-started.md)
2. [Plugin API Reference](api-reference.md)
3. [Manifest Format](manifest-format.md)
4. [Plugin Types](plugin-types.md)
   - [Theme Plugins](theme-plugins.md)
   - [Translation Plugins](translation-plugins.md)
   - [TTS Plugins](tts-plugins.md)
   - [Feature Plugins](feature-plugins.md)
5. [Packaging and Distribution](packaging.md)
6. [Testing Your Plugin](testing.md)
7. [Best Practices](best-practices.md)
8. [Submission Guidelines](submission-guidelines.md)
9. [Examples](../examples/)

## Quick Start

To create your first plugin:

1. Use the plugin template generator:
   ```bash
   ./gradlew generatePluginTemplate --type=theme --name=MyTheme
   ```

2. Implement your plugin logic in the generated files

3. Test your plugin:
   ```bash
   ./gradlew validatePlugin --plugin=MyTheme
   ```

4. Package your plugin:
   ```bash
   ./gradlew packagePlugin --plugin=MyTheme
   ```

5. Submit to the marketplace following the [submission guidelines](submission-guidelines.md)

## Support

- [API Documentation](api-reference.md)
- [Example Plugins](../examples/)
- [Developer Portal](https://plugins.ireader.app/developer)
- [Community Forum](https://forum.ireader.app/plugins)

## Requirements

- Kotlin 1.9.0 or higher
- IReader SDK 1.0.0 or higher
- Gradle 8.0 or higher
