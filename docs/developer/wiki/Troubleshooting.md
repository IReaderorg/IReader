# Troubleshooting

This guide helps you solve common problems you might encounter while using IReader.

## Extension Issues

### "Extension Not Secure" Warning

**Problem**: You see a warning that says "Extension is not secure" when installing.

**Solution**:
1. This is normal and expected - IReader extensions aren't signed with the Google Play Store certificate
2. Tap **Install Anyway** to proceed with installation
3. If the option doesn't appear, you may need to enable installation from unknown sources in your device settings

### Extensions Not Installing

**Problem**: Extensions won't install or installation fails.

**Solutions**:
1. **Enable Unknown Sources**:
   - Go to **Android Settings > Security > Unknown Sources**
   - Or for Android 8+: **Settings > Apps > Special access > Install unknown apps**
   - Enable for both IReader and your browser

2. **Check Android Version**:
   - Some extensions require a minimum Android version
   - Check extension details for compatibility information

3. **Clear Cache**:
   - Go to **Android Settings > Apps > IReader > Storage**
   - Tap **Clear Cache**
   - Try installing again

### Extensions Not Loading

3. **Check Source Status**:
   - The source might be down or changed
   - Try adding from a different source

### Library Takes Too Long to Load

**Problem**: Library is slow to load or books take a long time to appear.

**Solutions**:
1. **Reduce Library Size**:
   - Remove books you no longer need
   - Split books into categories

2. **Clear Cache**:
   - Go to **Settings > Advanced > Clear cache**
   - Restart the app

3. **Check Storage Space**:
   - Ensure your device has sufficient free storage
   - At least 500MB is recommended

## Reading Issues

### Chapters Not Loading

**Problem**: Chapters fail to load when reading.

**Solutions**:
1. **Check Internet Connection**:
   - Ensure you have a stable connection
   - Try switching networks

2. **Refresh Chapter**:
   - Pull down to refresh the chapter
   - Or tap the refresh icon in the menu

3. **Try Different Source**:
   - The current source might be down
   - Try adding the book from another source

### Reader Crashes

**Problem**: The app crashes while reading.

**Solutions**:
1. **Update App**:
   - Make sure you're using the latest version of IReader

2. **Clear Cache**:
   - Go to **Settings > Advanced > Clear cache**
   - Restart the app

3. **Check Memory**:
   - Close other apps running in the background
   - Restart your device

### Text Display Issues

**Problem**: Text appears too small, too large, or formatting is incorrect.

**Solutions**:
1. **Adjust Text Settings**:
   - While reading, tap the middle of the screen
   - Tap the text settings icon (A)
   - Adjust font size, font family, and line spacing

2. **Try Different Reader Mode**:
   - Switch between Paged and Continuous reading modes
   - Access from reading settings menu

## Backup and Restore Issues

### Backup Failed

**Problem**: Can't create a backup.

**Solutions**:
1. **Check Storage Permissions**:
   - Ensure IReader has permission to access storage
   - Go to **Settings > Apps > IReader > Permissions**

2. **Check Storage Space**:
   - Ensure you have sufficient free space
   - At least 100MB is recommended for backups

### Restore Failed

**Problem**: Can't restore from backup.

**Solutions**:
1. **Check Backup File**:
   - Ensure the backup file isn't corrupted
   - Try using a different backup file

2. **Check App Version**:
   - Backups may not be compatible between significantly different app versions
   - Try updating to the latest version

## Performance Issues

### App Running Slowly

**Problem**: IReader is sluggish or unresponsive.

**Solutions**:
1. **Clear Cache**:
   - Go to **Settings > Advanced > Clear cache**

2. **Reduce Library Size**:
   - Remove unnecessary books
   - Use categories to organize

3. **Limit Extensions**:
   - Disable extensions you don't use
   - Only keep essential ones enabled

4. **Check for Updates**:
   - Update to the latest version of the app
   - Update all extensions

## If All Else Fails

If you've tried the solutions above and still have issues:

1. **Full Reset**:
   - Make a backup first
   - Go to **Settings > Advanced > Clear database**
   - Restart the app and restore your backup

2. **Reinstall**:
   - Uninstall IReader
   - Reinstall from the original source
   - Restore from backup

3. **Report the Issue**:
   - Go to [GitHub Issues](https://github.com/IReaderorg/IReader/issues)
   - Search for similar issues or create a new one
   - Include detailed steps to reproduce the problem 