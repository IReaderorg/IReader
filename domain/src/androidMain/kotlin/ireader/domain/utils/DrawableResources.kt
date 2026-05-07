package ireader.domain.utils

/**
 * Helper object to get Android drawable resource IDs.
 * Uses Android system drawables as fallback since Compose resources can't be used in notifications.
 */
object DrawableResources {
    // Convenience properties for commonly used drawables
    val ic_downloading: Int = android.R.drawable.stat_sys_download
    val ic_infinity: Int = android.R.drawable.ic_dialog_info
    val ic_update: Int = android.R.drawable.stat_notify_sync
    val baseline_close_24: Int = android.R.drawable.ic_menu_close_clear_cancel
    val ic_baseline_skip_previous: Int = android.R.drawable.ic_media_previous
    val ic_baseline_pause: Int = android.R.drawable.ic_media_pause
    val ic_baseline_play_arrow: Int = android.R.drawable.ic_media_play
    val ic_baseline_skip_next: Int = android.R.drawable.ic_media_next
    val baseline_pause_24: Int = android.R.drawable.ic_media_pause
    val baseline_check_circle_24: Int = android.R.drawable.checkbox_on_background
    val baseline_error_24: Int = android.R.drawable.stat_notify_error
    val baseline_wifi_off_24: Int = android.R.drawable.ic_dialog_alert
    val baseline_signal_cellular_alt_24: Int = android.R.drawable.ic_dialog_info
    val baseline_settings_24: Int = android.R.drawable.ic_menu_preferences
    val baseline_storage_24: Int = android.R.drawable.ic_menu_save
    val baseline_warning_24: Int = android.R.drawable.stat_sys_warning
    val baseline_refresh_24: Int = android.R.drawable.ic_menu_rotate
    val ic_baseline_open_in_new_24: Int = android.R.drawable.ic_menu_view
    val ic_input_add: Int = android.R.drawable.ic_input_add
    val ic_media_previous: Int = android.R.drawable.ic_media_previous
    val ic_media_pause: Int = android.R.drawable.ic_media_pause
    val ic_media_play: Int = android.R.drawable.ic_media_play
    val ic_media_next: Int = android.R.drawable.ic_media_next
    val ic_menu_close_clear_cancel: Int = android.R.drawable.ic_menu_close_clear_cancel
    val stat_notify_sync: Int = android.R.drawable.stat_notify_sync
    val stat_sys_download: Int = android.R.drawable.stat_sys_download
}
