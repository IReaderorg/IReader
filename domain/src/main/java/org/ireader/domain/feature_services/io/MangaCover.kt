package org.ireader.domain.feature_services.io

import org.ireader.domain.models.entities.Book

class MangaCover(
  val id: Long,
  val sourceId: Long,
  val cover: String,
  val favorite: Boolean,
) {

    companion object {

        fun from(book: Book): MangaCover {
            return MangaCover(book.id, book.sourceId, book.cover, book.favorite)
        }
    }

}