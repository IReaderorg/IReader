package ireader.domain.usecases.database

/**
 * Use case for repairing database issues.
 * This abstracts the database repair functionality from the data layer.
 */
interface RepairDatabaseUseCase {
    /**
     * Repairs database issues by recreating views and validating structure
     * @throws Exception if repair fails
     */
    suspend fun execute()
}
