/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.source

import org.ireader.core_api.source.model.FilterList
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangasPageInfo

interface CatalogSource : Source {

  override val lang: String

  suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo

  suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo

  fun getListings(): List<Listing>

  fun getFilters(): FilterList

}
