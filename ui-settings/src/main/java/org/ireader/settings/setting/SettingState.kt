package org.ireader.settings.setting

data class SettingState(
    val
    doh: Int = 0,
    val dialogState: Boolean = false,
    val importMode: ImportMode = ImportMode.JavaMode,
)
enum class ImportMode {
    JavaMode,
    KotlinMode
}
