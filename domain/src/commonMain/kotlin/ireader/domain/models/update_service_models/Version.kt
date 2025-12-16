

package ireader.domain.models.update_service_models

import ireader.core.log.Log

data class Version(
    val version: String,
) {

    companion object {
        fun create(versionTag: String?): Version {
            val version = versionTag?.replace("[^\\d.]".toRegex(), "") ?: "0.0.0"
            Log.debug { "Version.create: input='$versionTag' -> cleaned='$version'" }
            return Version(version)
        }

        fun isNewVersion(versionTag: String?, currentVersion: String): Boolean {
            Log.info { "Version.isNewVersion: comparing remote='$versionTag' with current='$currentVersion'" }
            
            val newVersionStr = versionTag?.replace("[^\\d.]".toRegex(), "") ?: run {
                Log.warn { "Version.isNewVersion: versionTag is null or empty" }
                return false
            }
            val currentVersionStr = currentVersion.replace("[^\\d.]".toRegex(), "")
            
            Log.debug { "Version.isNewVersion: cleaned versions - remote='$newVersionStr', current='$currentVersionStr'" }

            // Parse version parts
            val newParts = newVersionStr.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = currentVersionStr.split(".").mapNotNull { it.toIntOrNull() }
            
            Log.debug { "Version.isNewVersion: parsed parts - remote=$newParts, current=$currentParts" }
            
            // Handle empty version parts
            if (newParts.isEmpty()) {
                Log.warn { "Version.isNewVersion: Could not parse remote version '$newVersionStr'" }
                return false
            }
            if (currentParts.isEmpty()) {
                Log.warn { "Version.isNewVersion: Could not parse current version '$currentVersionStr'" }
                return false
            }
            
            val maxLength = maxOf(newParts.size, currentParts.size)
            
            // Compare each version part
            for (i in 0 until maxLength) {
                val newPart = newParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0
                
                when {
                    newPart > currentPart -> {
                        Log.info { "Version.isNewVersion: NEW VERSION AVAILABLE (part $i: $newPart > $currentPart)" }
                        return true
                    }
                    newPart < currentPart -> {
                        Log.info { "Version.isNewVersion: Current is newer (part $i: $newPart < $currentPart)" }
                        return false
                    }
                    // Continue to next part if equal
                }
            }
            
            // All parts are equal, not a new version
            Log.info { "Version.isNewVersion: Versions are equal, no update needed" }
            return false
        }
    }
}
