package ireader.imageloader.coil.image_loaders

import coil.key.Keyer
import coil.request.Options
import ireader.domain.models.BookCover

class BookCoverKeyer : Keyer<BookCover> {
    override fun key(data: BookCover, options: Options): String {
           return "${data.cover};${data.lastModified}"
    }
}
