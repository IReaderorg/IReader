/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import ir.kazemcodes.infinityreader.Database
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


internal class JvmDatabaseHandler constructor(
  val db: Database,
  private val driver: SqlDriver,
  val queryDispatcher: CoroutineDispatcher = Dispatchers.IO,
  val transactionDispatcher: CoroutineDispatcher = queryDispatcher
) : DatabaseHandler {

  val suspendingTransactionId = ThreadLocal<Int>()

  override suspend fun <T> await(inTransaction: Boolean, block: suspend Database.() -> T): T {
    return dispatch(inTransaction, block)
  }

  override suspend fun <T : Any> awaitList(
    inTransaction: Boolean, block:
    suspend Database.() -> Query<T>
  ): List<T> {
    return dispatch(inTransaction) { block(db).executeAsList() }
  }

  override suspend fun <T : Any> awaitOne(
    inTransaction: Boolean, block:
    suspend Database.() -> Query<T>
  ): T {
    return dispatch(inTransaction) { block(db).executeAsOne() }
  }

  override suspend fun <T : Any> awaitOneOrNull(
    inTransaction: Boolean, block:
    suspend Database.() -> Query<T>
  ): T? {
    return dispatch(inTransaction) { block(db).executeAsOneOrNull() }
  }

  override fun <T : Any> subscribeToList(block: Database.() -> Query<T>): Flow<List<T>> {
    return block(db).asFlow().mapToList(queryDispatcher)
  }

  override fun <T : Any> subscribeToOne(block: Database.() -> Query<T>): Flow<T> {
    return block(db).asFlow().mapToOne(queryDispatcher)
  }

  override fun <T : Any> subscribeToOneOrNull(block: Database.() -> Query<T>): Flow<T?> {
    return block(db).asFlow().mapToOneOrNull(queryDispatcher)
  }

  private suspend fun <T> dispatch(inTransaction: Boolean, block: suspend Database.() -> T): T {
    // Create a transaction if needed and run the calling block inside it.
    if (inTransaction) {
      return withTransaction { block(db) }
    }

    // If we're currently in the transaction thread, there's no need to dispatch our query.
    if (driver.currentTransaction() != null) {
      return block(db)
    }

    // Get the current database context and run the calling block.
    val context = getCurrentDatabaseContext()
    return withContext(context) { block(db) }
  }

}
