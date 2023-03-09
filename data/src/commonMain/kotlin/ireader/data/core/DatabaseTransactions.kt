package ireader.data.core

import ireader.core.db.Transactions

internal class DatabaseTransactions  constructor(
  private val handler: DatabaseHandler
) : Transactions {

  override suspend fun <T> run(action: suspend () -> T): T {
    return handler.await(inTransaction = true) {
      action()
    }
  }

}
