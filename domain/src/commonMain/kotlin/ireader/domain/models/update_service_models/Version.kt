

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
            val newVersionStr = versionTag?.replace("[^\\d.]".toRegex(), "") ?: return false
            val currentVersionStr = currentVersion.replace("[^\\d.]".toRegex(), "")

            // Parse version parts
            val newParts = newVersionStr.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = currentVersionStr.split(".").map { it.toIntOrNull() ?: 0 }
            
            val maxLength = maxOf(newParts.size, currentParts.size)
            
            // Compare each version part
            for (i in 0 until maxLength) {
                val newPart = newParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0
                
                when {
                    newPart > currentPart -> return true
                    newPart < currentPart -> return false
                    // Continue to next part if equal
                }
            }
            
            // All parts are equal, not a new version
            return false
        }
    }
}
