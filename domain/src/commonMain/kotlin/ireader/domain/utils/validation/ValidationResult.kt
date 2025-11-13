package ireader.domain.utils.validation

/**
 * Represents the result of a validation operation
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Valid
    
    val isInvalid: Boolean
        get() = this is Invalid
    
    fun getAllErrors(): List<ValidationError> {
        return when (this) {
            is Valid -> emptyList()
            is Invalid -> errors
        }
    }
}

/**
 * Represents a single validation error
 */
data class ValidationError(
    val field: String,
    val message: String
)

/**
 * Combines multiple validation results
 */
fun combineValidationResults(vararg results: ValidationResult): ValidationResult {
    val allErrors = results.flatMap { it.getAllErrors() }
    return if (allErrors.isEmpty()) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(allErrors)
    }
}
