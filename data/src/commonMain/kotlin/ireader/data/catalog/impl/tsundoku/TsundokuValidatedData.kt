package ireader.data.catalog.impl.tsundoku

/**
 * Validated metadata from a Tsundoku extension APK.
 * Shared between Android and Desktop loaders.
 */
data class TsundokuValidatedData(
    val versionCode: Int,
    val versionName: String,
    val libVersion: Double,
    val isNovel: Boolean,
    val nsfw: Boolean,
    val classToLoad: String,
    val factoryClassName: String?
)
