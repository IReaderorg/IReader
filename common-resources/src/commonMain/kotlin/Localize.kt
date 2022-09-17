/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.common.resources

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource


@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun localize(resource: StringResource): String


@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun localize(resource: StringResource, vararg args: Any): String


@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun localizePlural(resource: PluralsResource, quantity: Int): String


@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun localizePlural(resource: PluralsResource, quantity: Int, vararg args: Any): String
