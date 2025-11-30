# Plugin Manifest Format

The plugin manifest (`plugin.json`) describes your plugin's metadata, capabilities, and requirements.

## Complete Manifest Example

```json
{
  "id": "com.example.myplugin",
  "name": "My Plugin",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "A comprehensive plugin for IReader",
  "author": {
    "name": "Developer Name",
    "email": "dev@example.com",
    "website": "https://example.com"
  },
  "type": "FEATURE",
  "permissions": [
    "NETWORK",
    "STORAGE",
    "READER_CONTEXT"
  ],
  "minIReaderVersion": "1.0.0",
  "platforms": ["ANDROID", "IOS", "DESKTOP"],
  "iconUrl": "icon.png",
  "screenshotUrls": [
    "screenshot1.png",
    "screenshot2.png"
  ],
  "monetization": {
    "type": "FREEMIUM",
    "features": [
      {
        "id": "premium_voices",
        "name": "Premium Voices",
        "description": "Access to high-quality voices",
        "price": 4.99,
        "currency": "USD"
      }
    ]
  }
}
```

## Field Reference

### Required Fields

#### `id` (string)
Unique identifier for your plugin. Use reverse domain notation.
- Format: `com.company.pluginname`
- Must be unique across all plugins
- Cannot be changed after publication

#### `name` (string)
Display name of your plugin.
- Maximum 50 characters
- Should be descriptive and unique

#### `version` (string)
Semantic version of your plugin.
- Format: `MAJOR.MINOR.PATCH`
- Example: `1.0.0`, `2.1.3`

#### `versionCode` (integer)
Numeric version for comparison.
- Must increment with each release
- Used for update detection

#### `description` (string)
Brief description of your plugin.
- Maximum 500 characters
- Should explain what the plugin does

#### `author` (object)
Information about the plugin developer.

```json
{
  "name": "Developer Name",
  "email": "dev@example.com",
  "website": "https://example.com"
}
```

- `name` (required): Developer or company name
- `email` (optional): Contact email
- `website` (optional): Developer website

#### `type` (string)
Plugin type. One of:
- `THEME` - Visual theme plugin
- `TRANSLATION` - Translation service plugin
- `TTS` - Text-to-speech plugin
- `FEATURE` - Custom feature plugin

#### `minIReaderVersion` (string)
Minimum IReader version required.
- Format: `MAJOR.MINOR.PATCH`
- Plugin won't install on older versions

#### `platforms` (array)
Supported platforms. One or more of:
- `ANDROID`
- `IOS`
- `DESKTOP`

### Optional Fields

#### `permissions` (array)
Required permissions. Available permissions:

- `NETWORK` - Access to network/internet
- `STORAGE` - Access to local storage
- `READER_CONTEXT` - Access to reading context
- `LIBRARY_ACCESS` - Access to user's library
- `PREFERENCES` - Access to app preferences
- `NOTIFICATIONS` - Show notifications

Example:
```json
"permissions": ["NETWORK", "STORAGE"]
```

#### `iconUrl` (string)
Path to plugin icon (relative to plugin package).
- Recommended size: 512x512px
- Format: PNG with transparency

#### `screenshotUrls` (array)
Paths to screenshot images.
- Recommended size: 1080x1920px
- Maximum 5 screenshots

#### `monetization` (object)
Monetization configuration.

##### Free Plugin
```json
"monetization": {
  "type": "FREE"
}
```

##### Premium Plugin
```json
"monetization": {
  "type": "PREMIUM",
  "price": 9.99,
  "currency": "USD",
  "trialDays": 7
}
```

- `price` (required): Plugin price
- `currency` (required): Currency code (USD, EUR, etc.)
- `trialDays` (optional): Trial period in days

##### Freemium Plugin
```json
"monetization": {
  "type": "FREEMIUM",
  "features": [
    {
      "id": "feature_id",
      "name": "Feature Name",
      "description": "Feature description",
      "price": 4.99,
      "currency": "USD"
    }
  ]
}
```

## Validation Rules

The manifest must pass these validation checks:

1. **ID Format**: Must match `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$`
2. **Version Format**: Must be valid semantic version
3. **Version Code**: Must be positive integer
4. **Type**: Must be valid plugin type
5. **Permissions**: Must be valid permission names
6. **Platforms**: Must contain at least one valid platform
7. **Min Version**: Must be valid semantic version
8. **Monetization**: Prices must be positive numbers

## Best Practices

1. **Choose a unique ID**: Use your domain name to avoid conflicts
2. **Version properly**: Follow semantic versioning
3. **Request minimal permissions**: Only request what you need
4. **Provide good descriptions**: Help users understand your plugin
5. **Include screenshots**: Show your plugin in action
6. **Set appropriate pricing**: Research similar plugins
7. **Support multiple platforms**: Reach more users
8. **Keep manifest updated**: Update with each release
