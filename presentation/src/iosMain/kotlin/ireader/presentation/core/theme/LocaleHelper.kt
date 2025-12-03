package ireader.presentation.core.theme

actual class LocaleHelper {
    actual fun setLocaleLang() {
        // iOS locale is managed by the system
    }

    actual fun updateLocal() {
        // iOS locale is managed by the system
    }

    actual fun resetLocale() {
        // iOS locale is managed by the system
    }

    actual fun getLocales() {
        // iOS locale is managed by the system
    }

    actual val languages: MutableList<String> = mutableListOf()
}
