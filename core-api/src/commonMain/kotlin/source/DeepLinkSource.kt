

package ireader.core.api.source

import ireader.core.api.source.model.DeepLink

interface DeepLinkSource : ireader.core.api.source.Source {

    fun handleLink(url: String): DeepLink?

    fun findMangaKey(chapterKey: String): String?
}
