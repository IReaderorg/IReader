package ir.kazemcodes.infinity.api_feature.data

import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter


data class BookPage(val Books: List<Book>, val hasNextPage: Boolean)
data class ChapterPage(val chapters: List<Chapter>, val hasNextPage: Boolean)