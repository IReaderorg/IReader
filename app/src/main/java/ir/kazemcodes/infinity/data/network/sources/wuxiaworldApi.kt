package ir.kazemcodes.infinity.data.network.sources

//class WuxiaorldApi: HttpSource() {
//    override val baseUrl: String
//        get() = "https://wuxiaworld.site/"
//    override val name: String
//        get() = "Wuxiaorld Api"
//    override val nextPageLinkFormat: String
//        get() = "https://wuxiaworld.site/page/{page}/"
//
//    override fun headersBuilder() = Headers.Builder().apply {
//        add(
//            "User-Agent",
//            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0 "
//        )
//        add("Referer", baseUrl)
//    }
//
//    override val id: Long
//        get() = 1
//    override val lang: String
//        get() = "en"
//
//    override suspend fun fetchElements(url: String, headers: Map<String, String>): Elements {
//
//        return kotlin.runCatching {
//            val request = GET(url)
//            val client = OkHttpClient()
//            val ok = client.newCall(request).await()
//            return@runCatching Jsoup.parse(ok.body?.string()?:"").allElements
//
//        }.getOrThrow()
//    }
//
//    override fun fetchBook(book: Book, elements: Elements): Book {
//        val eThumbnail =
//            elements.select(".summary_image a img").eachAttr("src")
//
//
//        val eAuthor = elements.select(".author-content a").eachText()
//        val eGenre = elements.select(".genres-content a").eachText().joinToString { " - " }
//        val eDescription =
//            elements.select(".description-summary p").eachText().joinToString("\n\n")
//
//        book.apply {
//            coverLink = eThumbnail[0]
//            author = eAuthor[0]
//            description = eDescription
//            category=eGenre
//        }
//        return book
//    }
//
//    override fun fetchBooks(elements: Elements): List<Book> {
//        Timber.d("TAG" + elements.text())
//        val books = mutableListOf<Book>()
//        val eTitles =
//            elements.select(".h5").eachText()
//        val eImages = elements.select(".page-item-detail a").eachAttr("src")
//        val eLinks =
//            elements.select(".h5 a").eachAttr("href")
//        for (i in 0 until eTitles.size) {
//            books.add(Book.create().apply {
//                bookName = eTitles[i].toString()
//                coverLink = eImages[i]
//                link = eLinks[i]
//            })
//
//        }
//        return books
//    }
//
//    override suspend fun fetchChapters(book: Book, elements: Elements): List<Chapter> {
//
//        TODO("Not yet implemented")
//    }
//
//    override fun fetchReadingContent(elements: Elements): String {
//        TODO("Not yet implemented")
//    }
//
//    override fun searchBook(query: String): List<Book> {
//        TODO("Not yet implemented")
//    }
//
//    override val supportsLatest: Boolean
//        get() = true
//
//}