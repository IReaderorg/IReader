package ireader.data.category

import ireader.domain.models.entities.CategoryAutoRule

val categoryAutoRuleMapper: (Long, Long, String, String, Boolean) -> CategoryAutoRule = 
    { id, categoryId, ruleType, value, isEnabled ->
        CategoryAutoRule(
            id = id,
            categoryId = categoryId,
            ruleType = CategoryAutoRule.RuleType.valueOf(ruleType),
            value = value,
            isEnabled = isEnabled,
        )
    }
