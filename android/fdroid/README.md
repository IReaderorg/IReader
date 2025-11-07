# F-Droid Flavor

This flavor is specifically designed for F-Droid distribution and removes all proprietary dependencies.

## What's Different

- **No Firebase**: Analytics and Crashlytics are completely removed
- **No Google Services**: The google-services plugin is not applied
- **No Auto-Updates**: `INCLUDE_UPDATER` is set to false

## Building

To build the F-Droid flavor:

```bash
./gradlew assembleFdroidRelease
```

Or for debug:

```bash
./gradlew assembleFdroidDebug
```

## Notes

- The `google-services.json` file in this directory is a dummy file to satisfy build requirements
- This flavor is 100% FOSS compatible and suitable for F-Droid submission
