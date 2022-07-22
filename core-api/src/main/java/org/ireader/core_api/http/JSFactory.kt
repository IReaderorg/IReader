

package org.ireader.core_api.http

import app.cash.quickjs.QuickJs

/**
 * A factory for creating instances of [JS].
 */

class JSFactory() {

    /**
     * Returns a new instance of [JS].
     */
    fun create(): JS {
        return JS(QuickJs.create())
    }
}
