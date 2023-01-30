//package ireader.presentation.ui.video.component.cores.player
//
//import android.net.Uri
//import okhttp3.OkHttpClient
//
//open class ExtractorLink(
//        open val client: OkHttpClient?,
//        open val name: String,
//        val url: String,
//        open val referer: String,
//        open val quality: Int,
//        open val isM3u8: Boolean = false,
//        open val headers: Map<String, String> = mapOf(),
//        /** Used for getExtractorVerifierJob() */
//    open val extractorData: String? = null,
//)  {
//    override fun toString(): String {
//        return "ExtractorLink(name=$name, url=$url, referer=$referer, isM3u8=$isM3u8)"
//    }
//}
//
//data class ExtractorUri(
//        val uri: Uri,
//        val name: String,
//
//        val basePath: String? = null,
//        val relativePath: String? = null,
//        val displayName: String? = null,
//
//        val id: Int? = null,
//        val parentId: Int? = null,
//        val episode: Int? = null,
//        val season: Int? = null,
//        val headerName: String? = null,
//)
//
//
///**
// * If your site has an unorthodox m3u8-like system where there are multiple smaller videos concatenated
// * use this.
// * */
//data class ExtractorLinkPlayList(
//        override val client: OkHttpClient,
//        override val name: String,
//        val playlist: List<PlayListItem>,
//        override val referer: String,
//        override val quality: Int,
//        override val isM3u8: Boolean = false,
//        override val headers: Map<String, String> = mapOf(),
//        /** Used for getExtractorVerifierJob() */
//        override val extractorData: String? = null,
//) : ExtractorLink(
//        client,
//        name,
//        // Blank as un-used
//        "",
//        referer,
//        quality,
//        isM3u8,
//        headers,
//        extractorData
//)
///**
// * For use in the ConcatenatingMediaSource.
// * If features are missing (headers), please report and we can add it.
// * @param durationUs use Long.toUs() for easier input
// * */
//data class PlayListItem(
//        val url: String,
//        val durationUs: Long,
//)
//
///**
// * Converts Seconds to MicroSeconds, multiplication by 1_000_000
// * */
//fun Long.toUs(): Long {
//    return this * 1_000_000
//}