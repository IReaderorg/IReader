
package ireader.common.models.entities





data class BrowseRemoteKey(
    var id: String,
    val previousPage: Int?,
    var nextPage: Int?,
    var lastUpdated: Long?,
)
