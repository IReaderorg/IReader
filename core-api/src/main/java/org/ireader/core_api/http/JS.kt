

package org.ireader.core_api.http

import app.cash.quickjs.QuickJs
import java.io.Closeable

/**
 * An implementation of [JS] to execute JavaScript code backed by the quickjs library.
 */
class JS internal constructor(private val engine: QuickJs) : Closeable {

    /**
     * Evaluates the given JavaScript [script] and returns its result as [String] or throws an
     * exception.
     */
    fun evaluateAsString(script: String): String {
        return engine.evaluate(script) as String
    }

    /**
     * Evaluates the given JavaScript [script] and returns its result as [Int] or throws an exception.
     */
    fun evaluateAsInt(script: String): Int {
        return engine.evaluate(script) as Int
    }

    /**
     * Evaluates the given JavaScript [script] and returns its result as [Double] or throws an
     * exception.
     */
    fun evaluateAsDouble(script: String): Double {
        return engine.evaluate(script) as Double
    }

    /**
     * Evaluates the given JavaScript [script] and returns its result as [Boolean] or throws an
     * exception.
     */
    fun evaluateAsBoolean(script: String): Boolean {
        return engine.evaluate(script) as Boolean
    }

    /**
     * Closes this instance. No evaluations can be made on this instance after calling this method.
     */
    override fun close() {
        engine.close()
    }
}
