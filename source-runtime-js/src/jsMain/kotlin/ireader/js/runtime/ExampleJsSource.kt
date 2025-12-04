//package ireader.js.runtime
//
//import ireader.core.source.Dependencies
//import ireader.core.source.SourceFactory
//import ireader.core.source.model.*
//import kotlin.js.JsExport
//
///**
// * Example source demonstrating how to create a JS-compatible source.
// *
// * This source can be loaded on iOS via JavaScriptCore.
// *
// * ## Usage
// *
// * 1. Create your source extending SourceFactory (or SimpleNovelSource)
// * 2. Create an init function with @JsExport
// * 3. Register the source with SourceRegistry
// *
// * ```kotlin
// * @JsExport
// * fun initMySource() {
// *     registerSource("my-source") { deps ->
// *         MySource(deps)
// *     }
// * }
// * ```
// */
//class ExampleJsSource(deps: Dependencies) : SourceFactory(deps) {
//
//    override val name: String = "Example JS Source"
//    override val baseUrl: String = "https://example.com"
//    override val lang: String = "en"
//    override val id: Long = 1234567890L
//
//    override val exploreFetchers = listOf(
//        BaseExploreFetcher(
//            key = "search",
//            endpoint = "/search?q={query}&page={page}",
//            selector = "div.novel-item",
//            nameSelector = "h3.title",
//            linkSelector = "a",
//            linkAtt = "href",
//            coverSelector = "img",
//            coverAtt = "src",
//            addBaseUrlToLink = true,
//            type = Type.Search
//        ),
//        BaseExploreFetcher(
//            key = "popular",
//            endpoint = "/popular?page={page}",
//            selector = "div.novel-item",
//            nameSelector = "h3.title",
//            linkSelector = "a",
//            linkAtt = "href",
//            coverSelector = "img",
//            coverAtt = "src",
//            addBaseUrlToLink = true,
//            type = Type.Others
//        )
//    )
//
//    override val detailFetcher = Detail(
//        nameSelector = "h1.novel-title",
//        coverSelector = "div.cover img",
//        coverAtt = "src",
//        descriptionSelector = "div.description",
//        authorBookSelector = "span.author",
//        categorySelector = "div.genres a",
//        statusSelector = "span.status",
//        addBaseurlToCoverLink = true,
//        onStatus = { text ->
//            when (text.lowercase()) {
//                "ongoing" -> MangaInfo.ONGOING
//                "completed" -> MangaInfo.COMPLETED
//                else -> MangaInfo.UNKNOWN
//            }
//        }
//    )
//
//    override val chapterFetcher = Chapters(
//        selector = "ul.chapter-list li",
//        nameSelector = "a",
//        linkSelector = "a",
//        linkAtt = "href",
//        addBaseUrlToLink = true,
//        reverseChapterList = true
//    )
//
//    override val contentFetcher = Content(
//        pageContentSelector = "div.chapter-content p"
//    )
//}
//
///**
// * Initialize the example source.
// * Call this from iOS after loading the JS file.
// */
//@JsExport
//@OptIn(ExperimentalJsExport::class)
//fun initExampleSource() {
//    registerSource("example-js-source") { deps ->
//        ExampleJsSource(deps.toDependencies())
//    }
//    console.log("Example JS Source registered")
//}
