

package ireader.domain.models.update_service_models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val id: Int,
    val name: String,
    @SerialName("tag_name")
    val tag_name: String? = null,
    @SerialName("html_url")
    val html_url: String,
    @SerialName("created_at")
    val created_at: String,
    val body: String? = null,
    val assets: List<ReleaseAsset> = emptyList(),
    val prerelease: Boolean = false,
    val draft: Boolean = false,
)

@Serializable
data class ReleaseAsset(
    val id: Int,
    val name: String,
    val size: Long,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("content_type")
    val contentType: String = "",
    @SerialName("download_count")
    val downloadCount: Int = 0,
)
