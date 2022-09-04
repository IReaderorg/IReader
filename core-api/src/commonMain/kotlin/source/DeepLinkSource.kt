

package org.ireader.core_api.source

import org.ireader.core_api.source.model.DeepLink

interface DeepLinkSource : org.ireader.core_api.source.Source {

    fun handleLink(url: String): DeepLink?

    fun findMangaKey(chapterKey: String): String?
}
