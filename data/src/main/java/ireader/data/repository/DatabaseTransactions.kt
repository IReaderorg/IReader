package ireader.data.repository

import androidx.room.withTransaction
import ireader.core.api.db.Transactions
import ireader.data.local.AppDatabase

class DatabaseTransactions(
    private val handler: AppDatabase
) : Transactions {

    override suspend fun <T> run(action: suspend () -> T): T {
        return handler.withTransaction {
            action()
        }
    }
}
