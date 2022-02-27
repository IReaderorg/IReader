/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.di

import toothpick.Toothpick

/**
 * The global scope for dependency injection that will provide all the application level components.
 */
actual object AppScope : Scope by Toothpick.openRootScope() {

    /**
     * Returns a new subscope inheriting the root scope.
     */
    actual fun subscope(any: Any): Scope {
        return openSubScope(any)
    }

    /**
     * Returns an instance of [T] from the root scope.
     */
    actual inline fun <reified T> getInstance(): T {
        return getInstance(T::class.java)
    }

}
