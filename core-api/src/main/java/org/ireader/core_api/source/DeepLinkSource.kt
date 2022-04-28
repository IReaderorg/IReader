

package org.ireader.core_api.source

import org.ireader.core_api.source.model.DeepLink

interface DeepLinkSource : Source {

    fun handleLink(url: String): DeepLink?

    fun findMangaKey(chapterKey: String): String?
}
