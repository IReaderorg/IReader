package ireader.domain.models.entities

import ireader.i18n.REPO_URL

data class ExtensionSource(
    val id: Long,
    val name: String,
    val key: String,
    val owner: String,
    val source: String,
    val username:String? = null,
    val password:String? = null,
    val lastUpdate:Long = 0,
    val isEnable:Boolean = true,
    val repositoryType: String = "IREADER", // "IREADER" or "LNREADER"
) {

    fun visibleName(): String {
        return when {
            id < 0 -> "IReader"
            else -> name
        }
    }
    
    fun isLNReaderRepository(): Boolean {
        return repositoryType.equals("LNREADER", ignoreCase = true)
    }
    
    fun isIReaderRepository(): Boolean {
        return repositoryType.equals("IREADER", ignoreCase = true)
    }

    companion object {
        fun default() : ExtensionSource {
            return ExtensionSource(
                id = -1,
                name = "IReader",
                key = "$REPO_URL/index.min.json",
                owner = "IReader",
                source = "https://github.com/IReaderorg/IReader-extensions",
                lastUpdate = 0,
                isEnable = true,
                repositoryType = "IREADER"
            )
        }
    }
}