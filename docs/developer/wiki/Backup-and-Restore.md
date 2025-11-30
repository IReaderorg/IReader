# Backup and Restore

Backing up your IReader data ensures you won't lose your library, reading progress, and settings. This guide covers how to create backups and restore them when needed.

## Creating a Backup

### Full Backup

A full backup includes your library, reading progress, categories, and app settings:

1. Open IReader
2. Go to **Settings**
3. Scroll down and tap on **Backup and Restore**
4. Tap **Create backup**
5. Choose what to include in the backup:
   - **Library**: Books in your library
   - **Categories**: Category settings and assignments
   - **Reading Progress**: Chapter and reading position for each book
   - **App Settings**: App preferences and themes
   - **Sources & Extensions**: Source settings (but not the extensions themselves)
6. Tap **Backup**
7. Choose a location to save the backup file
   - By default, backups are saved to `Internal Storage/IReader/backup/`
   - You can also select cloud storage if available

### Automatic Backups

You can set up automatic backups:

1. Go to **Settings > Backup and Restore**
2. Enable **Automatic backups**
3. Set the backup frequency (daily, weekly, monthly)
4. Choose what to include in automatic backups
5. Select a maximum number of backups to keep

## Restoring From a Backup

### Full Restore

To restore your data from a backup:

1. Open IReader
2. Go to **Settings > Backup and Restore**
3. Tap **Restore**
4. Browse to the location of your backup file
   - The default location is `Internal Storage/IReader/backup/`
5. Select the backup file
6. Choose what to restore:
   - **Library**
   - **Categories**
   - **Reading Progress**
   - **App Settings**
   - **Sources & Extensions**
7. Tap **Restore**
8. The app will restart after restoration is complete

### Restore From Google Drive or Other Cloud Storage

If you've stored your backup on cloud storage:

1. Go to **Settings > Backup and Restore**
2. Tap **Restore**
3. Tap the menu icon (three dots) in the file browser
4. Select your cloud storage service
5. Navigate to your backup file
6. Select and restore as above

## Transferring Data to a New Device

To transfer your IReader data to a new device:

1. Create a full backup on your old device
2. Transfer the backup file to your new device
   - Use cloud storage, email, USB transfer, etc.
3. Install IReader on your new device
4. Open IReader and go to **Settings > Backup and Restore**
5. Tap **Restore**
6. Browse to the location of your backup file
7. Select the backup file and restore

## Troubleshooting Backup and Restore

### Backup File Not Found

If you can't find your backup file:

1. Check the default location: `Internal Storage/IReader/backup/`
2. Look in your Downloads folder
3. Search your device for files with `.ireader.backup` extension

### Restore Failed

If restore fails:

1. Check if the backup file is corrupted
2. Try an older backup file
3. Make sure you have enough storage space
4. Ensure IReader has storage permissions
5. Restart the device and try again

### Extensions Missing After Restore

Extensions need to be reinstalled after restoring:

1. Go to **Extensions** section
2. Reinstall any extensions you were using
3. Your source settings will still be intact 