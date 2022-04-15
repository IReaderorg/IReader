/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.http

import app.cash.quickjs.QuickJs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A factory for creating instances of [JS].
 */
@Singleton
class JSFactory @Inject internal constructor() {

  /**
   * Returns a new instance of [JS].
   */
  fun create(): JS {
    return JS(QuickJs.create())
  }

}
