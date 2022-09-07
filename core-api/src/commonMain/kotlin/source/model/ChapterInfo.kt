

package ireader.core.api.source.model

data class ChapterInfo(
    var key: String,
    var name: String,
    var dateUpload: Long = 0,
    var number: Float = -1f,
    var scanlator: String = ""
)
