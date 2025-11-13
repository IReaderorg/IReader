package ireader.domain.utils.validation

/**
 * Common validation utilities for domain models
 */
object Validators {
    
    /**
     * Validates an email address
     */
    fun validateEmail(email: String, fieldName: String = "email"): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "Email cannot be empty"))
            )
            !email.contains("@") -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "Invalid email format"))
            )
            !email.contains(".") -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "Invalid email format"))
            )
            email.length < 5 -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "Email is too short"))
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validates a password
     */
    fun validatePassword(
        password: String,
        fieldName: String = "password",
        minLength: Int = 6
    ): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "Password cannot be empty"))
            )
            password.length < minLength -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "Password must be at least $minLength characters"))
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validates a URL
     */
    fun validateUrl(url: String, fieldName: String = "url"): ValidationResult {
        return when {
            url.isBlank() -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "URL cannot be empty"))
            )
            !url.startsWith("http://") && !url.startsWith("https://") -> ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "URL must start with http:// or https://"))
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validates a non-empty string
     */
    fun validateNotEmpty(value: String, fieldName: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult.Invalid(
                listOf(ValidationError(fieldName, "$fieldName cannot be empty"))
            )
        } else {
            ValidationResult.Valid
        }
    }
    
    /**
     * Validates a string length
     */
    fun validateLength(
        value: String,
        fieldName: String,
        minLength: Int? = null,
        maxLength: Int? = null
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        if (minLength != null && value.length < minLength) {
            errors.add(ValidationError(fieldName, "$fieldName must be at least $minLength characters"))
        }
        
        if (maxLength != null && value.length > maxLength) {
            errors.add(ValidationError(fieldName, "$fieldName must be at most $maxLength characters"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates a numeric range
     */
    fun validateRange(
        value: Number,
        fieldName: String,
        min: Number? = null,
        max: Number? = null
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val doubleValue = value.toDouble()
        
        if (min != null && doubleValue < min.toDouble()) {
            errors.add(ValidationError(fieldName, "$fieldName must be at least $min"))
        }
        
        if (max != null && doubleValue > max.toDouble()) {
            errors.add(ValidationError(fieldName, "$fieldName must be at most $max"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates that a value matches a pattern
     */
    fun validatePattern(
        value: String,
        fieldName: String,
        pattern: Regex,
        errorMessage: String = "Invalid format"
    ): ValidationResult {
        return if (pattern.matches(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                listOf(ValidationError(fieldName, errorMessage))
            )
        }
    }
}
