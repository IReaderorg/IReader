/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.http

import app.cash.quickjs.QuickJs
import java.io.Closeable

/**
 * An implementation of [JS] to execute JavaScript code backed by the quickjs library.
 */
actual class JS internal constructor(private val engine: QuickJs) : Closeable {

  /**
   * Evaluates the given JavaScript [script] and returns its result as [String] or throws an
   * exception.
   */
  actual fun evaluateAsString(script: String): String {
    return engine.evaluate(script) as String
  }

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Int] or throws an exception.
   */
  actual fun evaluateAsInt(script: String): Int {
    return engine.evaluate(script) as Int
  }

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Double] or throws an
   * exception.
   */
  actual fun evaluateAsDouble(script: String): Double {
    return engine.evaluate(script) as Double
  }

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Boolean] or throws an
   * exception.
   */
  actual fun evaluateAsBoolean(script: String): Boolean {
    return engine.evaluate(script) as Boolean
  }

  /**
   * Closes this instance. No evaluations can be made on this instance after calling this method.
   */
  actual override fun close() {
    engine.close()
  }

}
