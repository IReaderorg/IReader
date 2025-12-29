package ireader.data.category

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.CategoryAutoRuleRepository
import ireader.domain.models.entities.CategoryAutoRule
import kotlinx.coroutines.flow.Flow

class CategoryAutoRuleRepositoryImpl(
    private val handler: DatabaseHandler,
) : CategoryAutoRuleRepository {
    
    override fun subscribeAll(): Flow<List<CategoryAutoRule>> {
        return handler.subscribeToList {
            categoryautoruleQueries.findAll(categoryAutoRuleMapper)
        }
    }
    
    override fun subscribeByCategoryId(categoryId: Long): Flow<List<CategoryAutoRule>> {
        return handler.subscribeToList {
            categoryautoruleQueries.findByCategoryId(categoryId, categoryAutoRuleMapper)
        }
    }
    
    override suspend fun findAll(): List<CategoryAutoRule> {
        return handler.awaitList {
            categoryautoruleQueries.findAll(categoryAutoRuleMapper)
        }
    }
    
    override suspend fun findByCategoryId(categoryId: Long): List<CategoryAutoRule> {
        return handler.awaitList {
            categoryautoruleQueries.findByCategoryId(categoryId, categoryAutoRuleMapper)
        }
    }
    
    override suspend fun findEnabledRules(): List<CategoryAutoRule> {
        return handler.awaitList {
            categoryautoruleQueries.findEnabledRules(categoryAutoRuleMapper)
        }
    }
    
    override suspend fun findByRuleType(ruleType: CategoryAutoRule.RuleType): List<CategoryAutoRule> {
        return handler.awaitList {
            categoryautoruleQueries.findByRuleType(ruleType.name, categoryAutoRuleMapper)
        }
    }
    
    override suspend fun findByGenreValue(genreValue: String): List<CategoryAutoRule> {
        return handler.awaitList {
            categoryautoruleQueries.findByGenreValue(genreValue, categoryAutoRuleMapper)
        }
    }
    
    override suspend fun findBySourceValue(sourceValue: String): List<CategoryAutoRule> {
        return handler.awaitList {
            categoryautoruleQueries.findBySourceValue(sourceValue, categoryAutoRuleMapper)
        }
    }
    
    override suspend fun insert(rule: CategoryAutoRule): Long {
        var insertedId: Long = 0
        handler.await(inTransaction = true) {
            categoryautoruleQueries.insert(
                categoryId = rule.categoryId,
                ruleType = rule.ruleType.name,
                value = rule.value,
                isEnabled = rule.isEnabled,
            )
            insertedId = categoryautoruleQueries.selectLastInsertedRowId().executeAsOne()
        }
        return insertedId
    }
    
    override suspend fun insertAll(rules: List<CategoryAutoRule>) {
        handler.await(inTransaction = true) {
            rules.forEach { rule ->
                categoryautoruleQueries.insert(
                    categoryId = rule.categoryId,
                    ruleType = rule.ruleType.name,
                    value = rule.value,
                    isEnabled = rule.isEnabled,
                )
            }
        }
    }
    
    override suspend fun update(rule: CategoryAutoRule) {
        handler.await {
            categoryautoruleQueries.update(
                id = rule.id,
                categoryId = rule.categoryId,
                ruleType = rule.ruleType.name,
                value = rule.value,
                isEnabled = rule.isEnabled,
            )
        }
    }
    
    override suspend fun delete(id: Long) {
        handler.await {
            categoryautoruleQueries.delete(id)
        }
    }
    
    override suspend fun deleteByCategoryId(categoryId: Long) {
        handler.await {
            categoryautoruleQueries.deleteByCategoryId(categoryId)
        }
    }
    
    override suspend fun deleteAll() {
        handler.await {
            categoryautoruleQueries.deleteAll()
        }
    }
}
