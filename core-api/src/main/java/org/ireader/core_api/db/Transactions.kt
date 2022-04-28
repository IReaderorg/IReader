

package org.ireader.core_api.db

interface Transactions {

    suspend fun <T> run(action: suspend () -> T): T
}
