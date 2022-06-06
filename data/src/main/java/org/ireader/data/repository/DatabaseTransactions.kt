package org.ireader.data.repository

import androidx.room.withTransaction
import org.ireader.core_api.db.Transactions
import org.ireader.data.local.AppDatabase

class DatabaseTransactions(
    private val handler: AppDatabase
) : Transactions {

    override suspend fun <T> run(action: suspend () -> T): T {
        return handler.withTransaction {
            action()
        }
    }
}
