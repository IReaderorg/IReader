package ir.kazemcodes.infinity.domain.network.apis

//
//class RealWebnovel : HttpSource() {
//    override val baseUrl: String
//        get() = "https://readwebnovels.net/"
//    override val name: String
//        get() = "RealWebNovel.net"
//    override val nextPageLinkFormat: String
//        get() = "https://readwebnovels.net/page/{page}/"
//
//
//    override suspend fun fetchElements(url: String, headers: Map<String, String> ): Elements {
//        return kotlin.runCatching {
//            val request = GET(url)
//            val client = OkHttpClient()
//            val ok = client.newCall(request).await()
//            // Log.d("TAG", "onCreate: " + ok.body?.string())
//
//            return@runCatching Jsoup.parse(ok.body?.string()?:"").allElements
//
//        }.getOrThrow()
//
//    }
//
//    override fun fetchBook(book: Book, elements: Elements): Book {
//        val eThumbnail =
//            elements.select("div.tab-summary > div.summary_image > a > img").eachAttr("src")
//
//
//        val eAuthor = elements.select("div.author-content a").eachText()
//        val eGenre = elements.select("iv.summary-content > div.genres-content").eachText()
//        val eDescription =
//            elements.select("div.description-summary div.summary__content.show-more").eachText().joinToString("\n\n")
//
//        book.apply {
//            coverLink = eThumbnail[0]
//            author = eAuthor[0]
//            description = eDescription
//        }
//        return book
//    }
//
//
//    override fun fetchBooks(elements: Elements): List<Book> {
//        val books = mutableListOf<Book>()
//        val eTitle =
//            elements.select("div.item-summary > div.post-title.font-title > h3 > a").eachText()
//        val eImage = elements.select("div.site-content a img").eachAttr("src")
//        val eLink =
//            elements.select("div > div.item-summary > div.post-title.font-title > h3 > a")
//                .eachAttr("href")
//        for (i in 0..11) {
//            books.add(Book.create().apply {
//                bookName = eTitle[i].toString()
//                coverLink = eImage[i]
//                link = eLink[i]
//            })
//        }
//        return books
//    }
//
//    override suspend fun fetchChapters(book: Book, elements: Elements): List<Chapter> {
//        val chapters = mutableListOf<Chapter>()
//        val eChaptersTitle = elements.select("li.wp-Book-chapter     a").eachText()
//        val eChaptersUrl = elements.select("li.wp-Book-chapter     a").eachAttr("href")
//
//
//        eChaptersTitle.forEachIndexed { index, element ->
//            chapters.add(Chapter(
//                bookName = book.bookName,
//                title = element,
//                link = eChaptersUrl[index],
//                index = index,
//            ))
//        }
//        return chapters
//    }
//
//    override fun fetchReadingContent(elements: Elements): String {
//        return elements.select("div.entry-content > div > div > div > div.text-left > p").eachText().joinToString("\n\n\n")
//    }
//
//    override fun searchBook(query: String): List<Book> {
//        TODO("Not yet implemented")
//    }
//
//    override val id: Long
//        get() = 2
//    override val lang: String
//        get() = "en"
//
//    override val supportsLatest: Boolean
//        get() = true
//}





//    override  fun fetchBook(book: Book): Book {
//
//            val elements =
//                Jsoup.connect(book.url).header("Referer", "https://readwebnovels.net/").get()
//            Log.d(TAG, "fetchLatestBooksFromElement: $elements")
//
//
//    }


