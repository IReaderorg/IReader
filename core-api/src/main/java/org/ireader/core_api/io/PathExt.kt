

package org.ireader.core_api.io

import okio.Path

val Path.nameWithoutExtension
    get() = name.substringBeforeLast(".")

val Path.extension
    get() = name.substringAfterLast(".")
