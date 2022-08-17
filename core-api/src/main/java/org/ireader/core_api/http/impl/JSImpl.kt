

package org.ireader.core_api.http.impl

import app.cash.quickjs.QuickJs
import org.ireader.core_api.http.main.JS
import java.io.Closeable




/**
 * An implementation of [JS] to execute JavaScript code backed by the quickjs library.
 */
class JSImpl internal constructor(private val engine: QuickJs) : Closeable, JS {

    /**
     * Evaluates the given JavaScript [script] and returns its result as [String] or throws an
     * exception.
     */
    override fun evaluateAsString(script: String): String {
        return engine.evaluate(script) as String
    }

    /**
     * Evaluates the given JavaScript [script] and returns its result as [Int] or throws an exception.
     */
    override fun evaluateAsInt(script: String): Int {
        return engine.evaluate(script) as Int
    }

    /**
     * Evaluates the given JavaScript [script] and returns its result as [Double] or throws an
     * exception.
     */
    override fun evaluateAsDouble(script: String): Double {
        return engine.evaluate(script) as Double
    }

    /**
     * Evaluates the given JavaScript [script] and returns its result as [Boolean] or throws an
     * exception.
     */
    override fun evaluateAsBoolean(script: String): Boolean {
        return engine.evaluate(script) as Boolean
    }

    /**
     * Closes this instance. No evaluations can be made on this instance after calling this method.
     */
    override fun close() {
        engine.close()
    }
}
