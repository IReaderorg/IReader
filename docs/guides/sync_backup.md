# Sync & Backup Guide

Keep your library safe and synchronized across devices using Supabase Sync and local backups.

## Supabase Sync

IReader uses Supabase to sync your reading progress, library, and history across devices.

### Setting Up Sync
1.  Go to **Settings** → **Sync**.
2.  **Default Configuration**: By default, IReader uses a shared Supabase instance. You can use this immediately without setup.
3.  **Login**: Ensure you are logged in (if required by the instance).

### Custom Supabase Configuration
For advanced users who want full control over their data, you can host your own Supabase instance.

1.  Go to **Settings** → **Sync**.
2.  Toggle **Use Custom Supabase** to ON.
3.  Enter your Supabase project details:
    *   **Project URL**: Your Supabase project URL (e.g., `https://xyz.supabase.co`).
    *   **API Key**: Your `anon` public key.
4.  **Multi-Endpoint**: If you use separate projects for different data (e.g., one for books, one for progress), enable "Multi-Endpoint Configuration" and enter details for each.
5.  **Database Schema**: Tap "Database Schema" to see the required tables (`users`, `reading_progress`, `synced_books`, `synced_chapters`) if you are setting up a new project.

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
