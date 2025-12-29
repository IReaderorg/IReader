package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryAutoRuleRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.CategoryAutoRule
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing category auto-categorization rules.
 */
class ManageCategoryAutoRulesUseCase(
    private val categoryAutoRuleRepository: CategoryAutoRuleRepository,
    private val categoryRepository: CategoryRepository,
) {
    /**
     * Subscribe to all rules for a category
     */
    fun subscribeRulesForCategory(categoryId: Long): Flow<List<CategoryAutoRule>> {
        return categoryAutoRuleRepository.subscribeByCategoryId(categoryId)
    }
    
    /**
     * Subscribe to all rules
     */
    fun subscribeAllRules(): Flow<List<CategoryAutoRule>> {
        return categoryAutoRuleRepository.subscribeAll()
    }
    
    /**
     * Get all rules for a category
     */
    suspend fun getRulesForCategory(categoryId: Long): List<CategoryAutoRule> {
        return categoryAutoRuleRepository.findByCategoryId(categoryId)
    }
    
    /**
     * Get all enabled rules
     */
    suspend fun getEnabledRules(): List<CategoryAutoRule> {
        return categoryAutoRuleRepository.findEnabledRules()
    }
    
    /**
     * Add a genre-based rule to a category
     * 
     * @param categoryId The category to add the rule to
     * @param genre The genre name to match
     * @return Result with the created rule or error
     */
    suspend fun addGenreRule(categoryId: Long, genre: String): Result<CategoryAutoRule> {
        return runCatching {
            // Validate category exists
            val category = categoryRepository.get(categoryId)
                ?: throw IllegalArgumentException("Category not found")
            
            // Validate genre is not empty
            val trimmedGenre = genre.trim()
            if (trimmedGenre.isEmpty()) {
                throw IllegalArgumentException("Genre cannot be empty")
            }
            
            // Check for duplicate rule
            val existingRules = categoryAutoRuleRepository.findByCategoryId(categoryId)
            val isDuplicate = existingRules.any { 
                it.ruleType == CategoryAutoRule.RuleType.GENRE && 
                it.value.equals(trimmedGenre, ignoreCase = true)
            }
            if (isDuplicate) {
                throw IllegalArgumentException("A rule for this genre already exists in this category")
            }
            
            val rule = CategoryAutoRule(
                categoryId = categoryId,
                ruleType = CategoryAutoRule.RuleType.GENRE,
                value = trimmedGenre,
                isEnabled = true,
            )
            
            val id = categoryAutoRuleRepository.insert(rule)
            rule.copy(id = id)
        }
    }
    
    /**
     * Add a source-based rule to a category
     * 
     * @param categoryId The category to add the rule to
     * @param sourceId The source ID to match
     * @param sourceName Optional source name for display purposes
     * @return Result with the created rule or error
     */
    suspend fun addSourceRule(
        categoryId: Long, 
        sourceId: Long,
        sourceName: String? = null
    ): Result<CategoryAutoRule> {
        return runCatching {
            // Validate category exists
            val category = categoryRepository.get(categoryId)
                ?: throw IllegalArgumentException("Category not found")
            
            // Check for duplicate rule
            val existingRules = categoryAutoRuleRepository.findByCategoryId(categoryId)
            val isDuplicate = existingRules.any { 
                it.ruleType == CategoryAutoRule.RuleType.SOURCE && 
                it.value == sourceId.toString()
            }
            if (isDuplicate) {
                throw IllegalArgumentException("A rule for this source already exists in this category")
            }
            
            // Store source ID as value, optionally with name for display
            val value = sourceId.toString()
            
            val rule = CategoryAutoRule(
                categoryId = categoryId,
                ruleType = CategoryAutoRule.RuleType.SOURCE,
                value = value,
                isEnabled = true,
            )
            
            val id = categoryAutoRuleRepository.insert(rule)
            rule.copy(id = id)
        }
    }
    
    /**
     * Toggle a rule's enabled state
     */
    suspend fun toggleRule(ruleId: Long, enabled: Boolean): Result<Unit> {
        return runCatching {
            val rules = categoryAutoRuleRepository.findAll()
            val rule = rules.find { it.id == ruleId }
                ?: throw IllegalArgumentException("Rule not found")
            
            categoryAutoRuleRepository.update(rule.copy(isEnabled = enabled))
        }
    }
    
    /**
     * Delete a rule
     */
    suspend fun deleteRule(ruleId: Long): Result<Unit> {
        return runCatching {
            categoryAutoRuleRepository.delete(ruleId)
        }
    }
    
    /**
     * Delete all rules for a category
     */
    suspend fun deleteRulesForCategory(categoryId: Long): Result<Unit> {
        return runCatching {
            categoryAutoRuleRepository.deleteByCategoryId(categoryId)
        }
    }
}
