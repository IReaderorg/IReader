package org.ireader.domain.models.update_service_models

import androidx.annotation.Keep

@Keep
data class Version(
    val version: String,
) {

    companion object {
        fun create(versionTag: String): Version {
            val version = versionTag.replace("[^\\d.]".toRegex(), "")
            return Version(version)
        }

        fun isNewVersion(versionTag: String, currentVersion: String): Boolean {
            val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")

            return newVersion != currentVersion
        }
    }

}