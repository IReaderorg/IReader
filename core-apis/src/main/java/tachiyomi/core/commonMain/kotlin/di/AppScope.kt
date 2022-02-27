/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.di

/**
 * The global scope for dependency injection that will provide all the application level components.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppScope {

    /**
     * Returns a new subscope inheriting the root scope.
     */
    fun subscope(any: Any): Scope

    /**
     * Returns an instance of [T] from the root scope.
     */
    inline fun <reified T> getInstance(): T

}
