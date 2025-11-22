

package ireader.domain.models.update_service_models

data class Version(
    val version: String,
) {

    companion object {
        fun create(versionTag: String?): Version {
            val version = versionTag?.replace("[^\\d.]".toRegex(), "") ?: "0.0.0"
            return Version(version)
        }

        fun isNewVersion(versionTag: String?, currentVersion: String): Boolean {
            val newVersion = versionTag?.replace("[^\\d.]".toRegex(), "") ?: return false

            return newVersion != currentVersion
        }
    }
}
