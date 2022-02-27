/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.di

/**
 * A child scope of [AppScope] which can be injected another modules and is usually short-lived.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect interface Scope

/**
 * Closes this scope.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun Scope.close()
