

package org.ireader.core_api.io

import okio.Path

fun Path.setLastModified(epoch: Long) {
    toFile().setLastModified(epoch)
}
