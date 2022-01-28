package ir.kazemcodes.infinity.feature_services.updater_service.models

import ir.kazemcodes.infinity.BuildConfig

data class Version(
    val version: String,
)  {

    companion object {
        fun create(versionTag: String): Version {
            val version = versionTag.replace("[^\\d.]".toRegex(), "")
            return Version(version)
        }
        fun isNewVersion(versionTag: String) : Boolean {
            val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")


            return newVersion != BuildConfig.VERSION_NAME
        }
    }

}