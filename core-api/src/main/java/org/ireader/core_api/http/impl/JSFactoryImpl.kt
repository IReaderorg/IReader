package org.ireader.core_api.http.impl

import app.cash.quickjs.QuickJs

import org.ireader.core_api.http.main.JS
import org.ireader.core_api.http.main.JSFactory

/**
 * A factory for creating instances of [JS].
 */

class JSFactoryImpl : JSFactory {

    /**
     * Returns a new instance of [JS].
     */
    override fun create(): JS {
        return JSImpl(QuickJs.create())
    }
}