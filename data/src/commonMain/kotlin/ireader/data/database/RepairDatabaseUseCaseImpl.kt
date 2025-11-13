package ireader.data.database

import ireader.data.core.DatabaseHandler
import ireader.domain.usecases.database.RepairDatabaseUseCase

/**
 * Implementation of RepairDatabaseUseCase
 */
class RepairDatabaseUseCaseImpl(
    private val databaseHandler: DatabaseHandler
) : RepairDatabaseUseCase {
    
    override suspend fun execute() {
        databaseHandler.repairDatabase()
    }
}
