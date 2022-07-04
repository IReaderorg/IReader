package org.ireader.image_loader.coil.image_loaders

import coil.key.Keyer
import coil.request.Options
import org.ireader.image_loader.BookCover

class BookCoverKeyer : Keyer<BookCover> {
    override fun key(data: BookCover, options: Options): String {
        return data.cover
    }
}
