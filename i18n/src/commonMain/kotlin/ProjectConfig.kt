package ireader.i18n

data class ProjectConfig(
    val commitCount: String,
    val commitSHA: String,
    val buildTime: String,
    val includeUpdater: Boolean,
    val preview: Boolean,
    val versionName: String,
    val versionCode: Int,
    val applicationId: String = "ir.kazemcodes.infinityreader"
)
