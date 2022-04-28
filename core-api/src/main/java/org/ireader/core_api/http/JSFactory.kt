

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
