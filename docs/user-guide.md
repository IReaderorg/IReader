# JavaScript Plugin User Guide

## Introduction

IReader supports JavaScript-based plugins from the LNReader ecosystem, giving you access to hundreds of novel sources. This guide will help you install, manage, and troubleshoot plugins.

## Table of Contents

1. [Installing Plugins](#installing-plugins)
2. [Managing Plugins](#managing-plugins)
3. [Updating Plugins](#updating-plugins)
4. [Using Filters](#using-filters)
5. [Troubleshooting](#troubleshooting)
6. [FAQ](#faq)

---

## Installing Plugins

### Method 1: From Plugin Repository (Recommended)

1. Open IReader
2. Go to **Settings** → **Extensions** → **JavaScript Plugins**
3. Tap **Browse Plugins**
4. Find the plugin you want to install
5. Tap **Install**
6. Wait for the download to complete
7. The plugin will appear in your sources list

### Method 2: Manual Installation (Android)

1. Download the `.js` plugin file to your device
2. Open IReader
3. Go to **Settings** → **Extensions** → **JavaScript Plugins**
4. Tap **Install from File**
5. Navigate to the downloaded `.js` file
6. Select the file
7. The plugin will be installed and appear in your sources list

**Plugin Location (Android):**
```
/data/data/com.ireader/files/js-plugins/
```

### Method 3: Manual Installation (Desktop)

1. Download the `.js` plugin file
2. Copy the file to the plugins directory:
   - **Windows**: `C:\Users\YourName\.ireader\js-plugins\`
   - **macOS**: `/Users/YourName/.ireader/js-plugins/`
   - **Linux**: `/home/yourname/.ireader/js-plugins/`
3. Restart IReader or tap **Refresh Sources**
4. The plugin will appear in your sources list

---

## Managing Plugins

### Viewing Installed Plugins

1. Go to **Browse** → **Sources**
2. JavaScript plugins are marked with a **JS** badge
3. Tap on a plugin to see its details:
   - Name and version
   - Source website
   - Language
   - Last update date

### Enabling/Disabling Plugins

1. Go to **Settings** → **Extensions** → **JavaScript Plugins**
2. Find the plugin in the list
3. Toggle the switch to enable/disable
4. Disabled plugins won't appear in the sources list

### Uninstalling Plugins

1. Go to **Settings** → **Extensions** → **JavaScript Plugins**
2. Find the plugin you want to remove
3. Tap the plugin
4. Tap **Uninstall**
5. Confirm the action

**Note:** Uninstalling a plugin will also remove its cached data and settings.

### Plugin Settings

Some plugins may have configurable settings:

1. Go to **Browse** → **Sources**
2. Long-press on the plugin
3. Select **Plugin Settings**
4. Adjust settings as needed
5. Tap **Save**

---

## Updating Plugins

### Automatic Updates

By default, IReader checks for plugin updates daily:

1. Go to **Settings** → **Extensions** → **JavaScript Plugins**
2. Ensure **Auto-update plugins** is enabled
3. IReader will check for updates every 24 hours
4. You'll receive a notification when updates are available

### Manual Update Check

1. Go to **Settings** → **Extensions** → **JavaScript Plugins**
2. Tap **Check for Updates**
3. If updates are available, you'll see a list
4. Tap **Update All** or update individual plugins

### Update Notifications

When updates are available:

1. You'll see a notification badge on the Extensions icon
2. Tap the notification to view available updates
3. Review the changelog (if available)
4. Tap **Update** to install

### Rollback

If an update causes issues:

1. Go to **Settings** → **Extensions** → **JavaScript Plugins**
2. Find the problematic plugin
3. Tap the plugin
4. Tap **Rollback to Previous Version**
5. The previous version will be restored

---

## Using Filters

Many plugins support filters to refine your search:

### Accessing Filters

1. Go to **Browse** → **Sources**
2. Select a JavaScript plugin
3. Tap the **Filter** icon (funnel icon)
4. The filter panel will appear

### Filter Types

#### Dropdown (Picker)

Select one option from a list:
- **Status**: All, Ongoing, Completed, Hiatus
- **Sort By**: Latest, Popular, Rating, Views

#### Text Input

Enter free text:
- **Author Name**: Type the author's name
- **Keyword**: Enter search keywords

#### Checkboxes

Select multiple options:
- **Genres**: Action, Romance, Fantasy, etc.
- Check the boxes for genres you want to include

#### Include/Exclude Checkboxes

Advanced filtering with three states:
- **Unchecked**: Ignore this option
- **Green Check**: Include this genre
- **Red X**: Exclude this genre

### Applying Filters

1. Set your desired filter values
2. Tap **Apply**
3. The novel list will refresh with filtered results
4. Tap **Reset** to clear all filters

### Saving Filter Presets

1. Configure your filters
2. Tap the **Save** icon
3. Enter a preset name (e.g., "Completed Fantasy")
4. Tap **Save**
5. Access saved presets from the filter menu

---

## Troubleshooting

### Plugin Won't Load

**Symptoms:**
- Plugin doesn't appear in sources list
- Error message when opening plugin

**Solutions:**

1. **Check plugin file:**
   - Ensure the `.js` file is not corrupted
   - Re-download the plugin from a trusted source

2. **Check plugin compatibility:**
   - Go to **Settings** → **Extensions** → **JavaScript Plugins**
   - Find the plugin in the list
   - Check if it shows "Incompatible" or "Error"
   - Update to the latest version

3. **Clear plugin cache:**
   - Go to **Settings** → **Extensions** → **JavaScript Plugins**
   - Tap the plugin
   - Tap **Clear Cache**
   - Restart IReader

4. **Check logs:**
   - Go to **Settings** → **Advanced** → **Debug Mode**
   - Enable **Plugin Debug Logging**
   - Try loading the plugin again
   - Go to **Settings** → **Advanced** → **View Logs**
   - Look for errors related to the plugin

### Plugin is Slow

**Symptoms:**
- Long loading times
- App becomes unresponsive

**Solutions:**

1. **Clear plugin cache:**
   - Old cached data can slow down plugins
   - Clear cache as described above

2. **Check network connection:**
   - Slow internet can affect plugin performance
   - Try switching between Wi-Fi and mobile data

3. **Reduce concurrent operations:**
   - Go to **Settings** → **Extensions** → **JavaScript Plugins**
   - Tap **Advanced Settings**
   - Reduce **Max Concurrent Executions** to 3

4. **Increase timeout:**
   - Go to **Settings** → **Extensions** → **JavaScript Plugins**
   - Tap **Advanced Settings**
   - Increase **Execution Timeout** to 45 seconds

### Novels Won't Load

**Symptoms:**
- Empty novel list
- "No results found" message

**Solutions:**

1. **Check source website:**
   - The source website might be down
   - Visit the website in a browser to verify

2. **Update plugin:**
   - The website structure may have changed
   - Update to the latest plugin version

3. **Check filters:**
   - Your filter combination might be too restrictive
   - Reset filters and try again

4. **Clear cache:**
   - Cached data might be outdated
   - Clear plugin cache

### Chapters Won't Load

**Symptoms:**
- Chapter list is empty
- Chapter content doesn't display

**Solutions:**

1. **Refresh novel details:**
   - Pull down to refresh on the novel details page
   - This will fetch the latest chapter list

2. **Check source website:**
   - The chapter might have been removed
   - Verify on the source website

3. **Clear chapter cache:**
   - Go to the novel details page
   - Tap the menu icon (three dots)
   - Select **Clear Chapter Cache**

### Images Won't Load

**Symptoms:**
- Novel covers don't display
- Chapter images are broken

**Solutions:**

1. **Check image headers:**
   - Some websites require specific headers
   - The plugin should handle this automatically
   - If not, report to the plugin developer

2. **Check network:**
   - Images might be blocked by your network
   - Try a different network or VPN

3. **Clear image cache:**
   - Go to **Settings** → **Advanced**
   - Tap **Clear Image Cache**

### Plugin Crashes

**Symptoms:**
- App crashes when using plugin
- "Plugin execution failed" error

**Solutions:**

1. **Update plugin:**
   - Ensure you have the latest version

2. **Reinstall plugin:**
   - Uninstall the plugin
   - Restart IReader
   - Reinstall the plugin

3. **Report the issue:**
   - Go to **Settings** → **About**
   - Tap **Report Issue**
   - Include:
     - Plugin name and version
     - Steps to reproduce
     - Error logs (if available)

---

## FAQ

### Q: Are JavaScript plugins safe?

**A:** Yes, plugins run in a sandboxed environment with restricted access. They cannot:
- Access your file system (except their own storage)
- Execute system commands
- Access other apps' data
- Make unauthorized network requests

However, only install plugins from trusted sources.

### Q: How many plugins can I install?

**A:** There's no hard limit, but performance may degrade with many plugins. We recommend:
- **Mobile**: 10-20 plugins
- **Desktop**: 30-50 plugins

### Q: Do plugins work offline?

**A:** Plugins require internet to fetch novel data. However:
- Cached data can be accessed offline
- Previously loaded chapters remain available
- Plugin code itself doesn't require internet

### Q: Can I create my own plugins?

**A:** Yes! See the [Plugin Development Guide](js-plugin-system.md) for details.

### Q: Why is my plugin not updating?

**A:** Check:
1. Auto-update is enabled in settings
2. You have an internet connection
3. The plugin repository is accessible
4. The plugin has a newer version available

### Q: Can I use plugins from other apps?

**A:** IReader uses the LNReader plugin format. Plugins from LNReader should work directly. Plugins from other apps may need conversion.

### Q: How do I backup my plugins?

**A:** 
1. Go to **Settings** → **Backup & Restore**
2. Tap **Backup Plugins**
3. Choose a location to save the backup
4. The backup includes:
   - Plugin files
   - Plugin settings
   - Cached data (optional)

### Q: How do I restore plugins from backup?

**A:**
1. Go to **Settings** → **Backup & Restore**
2. Tap **Restore Plugins**
3. Select your backup file
4. Choose what to restore:
   - Plugin files
   - Settings
   - Cached data
5. Tap **Restore**

### Q: What's the difference between JavaScript and native plugins?

**A:**

| Feature | JavaScript Plugins | Native Plugins |
|---------|-------------------|----------------|
| Installation | Easy (just copy .js file) | Requires app update |
| Updates | Independent, frequent | With app updates |
| Performance | Slightly slower | Faster |
| Development | Easier (JavaScript) | Harder (Kotlin) |
| Sandboxing | Yes | No (trusted code) |
| Availability | Hundreds available | Limited |

### Q: Can I disable JavaScript plugins entirely?

**A:** Yes:
1. Go to **Settings** → **Extensions** → **JavaScript Plugins**
2. Toggle **Enable JavaScript Plugins** off
3. All JS plugins will be hidden from sources

### Q: How much storage do plugins use?

**A:** 
- Plugin file: 10-50 KB each
- Cached data: Varies (can be cleared)
- Total: Usually < 10 MB for 20 plugins

### Q: Do plugins drain battery?

**A:** Minimal impact. Plugins only run when:
- Browsing novels
- Searching
- Loading chapters

They don't run in the background.

---

## Getting Help

If you're still having issues:

1. **Check the wiki**: [IReader Wiki](https://github.com/IReader/IReader/wiki)
2. **Ask the community**: [Discord Server](https://discord.gg/ireader)
3. **Report a bug**: [GitHub Issues](https://github.com/IReader/IReader/issues)

When reporting issues, include:
- IReader version
- Plugin name and version
- Device/OS information
- Steps to reproduce
- Error logs (if available)

---

## Additional Resources

- [Plugin Development Guide](js-plugin-system.md)
- [LNReader Plugin Repository](https://github.com/LNReader/lnreader-plugins)
- [Plugin API Reference](js-plugin-system.md#jsengine-api)
- [Example Plugins](examples/)

---

**Last Updated:** November 2024  
**IReader Version:** 1.0.0+
