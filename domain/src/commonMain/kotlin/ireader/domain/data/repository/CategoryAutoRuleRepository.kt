package ireader.domain.data.repository

import ireader.domain.models.entities.CategoryAutoRule
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for category auto-categorization rules.
 */
interface CategoryAutoRuleRepository {
    
    /**
     * Subscribe to all rules
     */
    fun subscribeAll(): Flow<List<CategoryAutoRule>>
    
    /**
     * Subscribe to rules for a specific category
     */
    fun subscribeByCategoryId(categoryId: Long): Flow<List<CategoryAutoRule>>
    
    /**
     * Get all rules
     */
    suspend fun findAll(): List<CategoryAutoRule>
    
    /**
     * Get rules for a specific category
     */
    suspend fun findByCategoryId(categoryId: Long): List<CategoryAutoRule>
    
    /**
     * Get all enabled rules
     */
    suspend fun findEnabledRules(): List<CategoryAutoRule>
    
    /**
     * Get enabled rules by rule type
     */
    suspend fun findByRuleType(ruleType: CategoryAutoRule.RuleType): List<CategoryAutoRule>
    
    /**
     * Find rules that match a specific genre
     */
    suspend fun findByGenreValue(genreValue: String): List<CategoryAutoRule>
    
    /**
     * Find rules that match a specific source
     */
    suspend fun findBySourceValue(sourceValue: String): List<CategoryAutoRule>
    
    /**
     * Insert a new rule
     * @return The ID of the inserted rule
     */
    suspend fun insert(rule: CategoryAutoRule): Long
    
    /**
     * Insert multiple rules
     */
    suspend fun insertAll(rules: List<CategoryAutoRule>)
    
    /**
     * Update an existing rule
     */
    suspend fun update(rule: CategoryAutoRule)
    
    /**
     * Delete a rule by ID
     */
    suspend fun delete(id: Long)
    
    /**
     * Delete all rules for a category
     */
    suspend fun deleteByCategoryId(categoryId: Long)
    
    /**
     * Delete all rules
     */
    suspend fun deleteAll()
}
