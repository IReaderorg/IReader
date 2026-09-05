# Sync & Backup Guide

Keep your library safe and synchronized across devices using Supabase Sync and local backups.

## Supabase Sync

IReader uses your personal, private Supabase database to sync your reading progress, library, and Spirit Stones across all devices.

> [!TIP]
> For complete step-by-step instructions on creating your free Supabase project, getting API keys, and setting up database tables, see the **[Supabase Cloud Sync & Setup Guide](supabase_setup_guide.md)**.

### Setting Up Personal Sync (Recommended)
1. Go to **Settings** → **Sync** → **Supabase Configuration**.
2. Enter your **Project URL** and **API Key** (`anon` key).
3. Tap **Save Configuration**, then tap **Test Connection**.
4. Use **Copy Setup SQL** in the app to get the database schema, then paste it into your Supabase SQL Editor.
5. Use **Share Config** to quickly export and import your setup across multiple devices.

### Sync Settings
*   **Auto Sync**: Automatically syncs progress in the background.
*   **WiFi Only**: Restricts sync to WiFi networks to save data.
*   **Sync Now**: Manually triggers a sync.

---

## Backup & Restore

Create local backups of your library to keep your data safe.

### Creating a Backup
1.  Go to **Settings** → **Backup & Restore**.
2.  Tap **Create Backup**.
3.  Select what to backup:
    *   **Library**: Your list of books and categories.
    *   **Settings**: App preferences.
    *   **History**: Read history.
4.  Tap **Backup** and choose a location to save the `.ireader` file.

### Restoring a Backup
1.  Go to **Settings** → **Backup & Restore**.
2.  Tap **Restore Backup**.
3.  Select the `.ireader` backup file.
4.  Confirm the restore.

> [!WARNING]
> Restoring a backup will overwrite your current library and settings.
