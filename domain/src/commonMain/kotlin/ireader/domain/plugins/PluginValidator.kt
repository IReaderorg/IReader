package ireader.domain.plugins

import ireader.plugin.api.PluginMonetization as ApiPluginMonetization

/**
 * Validates plugin manifests and ensures compatibility
 * Requirements: 1.1, 1.2, 1.3, 1.4, 17.1, 17.2, 17.3, 17.4, 17.5
 */
class PluginValidator(
    private val currentIReaderVersion: String,
    private val currentPlatform: Platform
) {
    /**
     * Validate a plugin manifest
     * @return Result.success if valid, Result.failure with PluginError if invalid
     */
    fun validate(manifest: PluginManifest): Result<Unit> {
        return try {
            // Validate version format
            validateVersion(manifest.version).getOrThrow()
            
            // Validate minimum IReader version compatibility
            validateMinVersion(manifest.minIReaderVersion).getOrThrow()
            
            // Validate permissions
            validatePermissions(manifest.permissions).getOrThrow()
            
            // Validate monetization if present
            manifest.monetization?.let { 
                validateMonetization(it).getOrThrow()
            }
            
            // Validate platform compatibility
            validatePlatformCompatibility(manifest.platforms).getOrThrow()
            
            // Validate manifest fields
            validateManifestFields(manifest).getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate version follows semantic versioning (semver)
     * Format: MAJOR.MINOR.PATCH (e.g., 1.0.0, 2.1.3)
     */
    fun validateVersion(version: String): Result<Unit> {
        val semverPattern = Regex("""^\d+\.\d+\.\d+(-[a-zA-Z0-9.-]+)?(\+[a-zA-Z0-9.-]+)?$""")
        
        return if (semverPattern.matches(version)) {
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalArgumentException("Invalid version format: $version. Expected semantic versioning (e.g., 1.0.0)")
            )
        }
    }
    
    /**
     * Validate minimum IReader version requirement
     */
    private fun validateMinVersion(minVersion: String): Result<Unit> {
        // First validate the format
        validateVersion(minVersion).getOrElse { 
            return Result.failure(
                IllegalArgumentException("Invalid minIReaderVersion format: $minVersion")
            )
        }
        
        // Compare versions
        val isCompatible = compareVersions(currentIReaderVersion, minVersion) >= 0
        
        return if (isCompatible) {
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalStateException("Incompatible version: requires $minVersion, current is $currentIReaderVersion")
            )
        }
    }
    
    /**
     * Validate plugin permissions
     */
    fun validatePermissions(permissions: List<PluginPermission>): Result<Unit> {
        // Check for duplicate permissions
        val duplicates = permissions.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            return Result.failure(
                IllegalArgumentException("Duplicate permissions found: ${duplicates.keys.joinToString()}")
            )
        }
        
        // All permissions are valid enum values, so no need to check validity
        return Result.success(Unit)
    }
    
    /**
     * Validate monetization configuration
     */
    fun validateMonetization(monetization: PluginMonetization): Result<Unit> {
        return when (monetization) {
            is ApiPluginMonetization.Premium -> {
                val trialDays = monetization.trialDays
                when {
                    monetization.price < 0 -> {
                        Result.failure(IllegalArgumentException("Premium price cannot be negative"))
                    }
                    monetization.currency.isBlank() -> {
                        Result.failure(IllegalArgumentException("Premium currency cannot be empty"))
                    }
                    trialDays != null && trialDays < 0 -> {
                        Result.failure(IllegalArgumentException("Trial days cannot be negative"))
                    }
                    else -> {
                        Result.success(Unit)
                    }
                }
            }
            is ApiPluginMonetization.Freemium -> {
                if (monetization.features.isEmpty()) {
                    Result.failure(IllegalArgumentException("Freemium plugin must have at least one premium feature"))
                } else {
                    // Validate each premium feature
                    for (feature in monetization.features) {
                        if (feature.price < 0) {
                            return Result.failure(
                                IllegalArgumentException("Feature '${feature.name}' price cannot be negative")
                            )
                        }
                        if (feature.currency.isBlank()) {
                            return Result.failure(
                                IllegalArgumentException("Feature '${feature.name}' currency cannot be empty")
                            )
                        }
                        if (feature.id.isBlank()) {
                            return Result.failure(
                                IllegalArgumentException("Feature ID cannot be empty")
                            )
                        }
                    }
                    Result.success(Unit)
                }
            }
            ApiPluginMonetization.Free -> Result.success(Unit)
        }
    }
    
    /**
     * Validate platform compatibility
     */
    fun validatePlatformCompatibility(platforms: List<Platform>): Result<Unit> {
        if (platforms.isEmpty()) {
            return Result.failure(
                IllegalArgumentException("Plugin must support at least one platform")
            )
        }
        
        // Check if current platform is supported
        if (!platforms.contains(currentPlatform)) {
            return Result.failure(
                IllegalStateException("Plugin does not support current platform: $currentPlatform")
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Validate required manifest fields
     */
    private fun validateManifestFields(manifest: PluginManifest): Result<Unit> {
        when {
            manifest.id.isBlank() -> 
                return Result.failure(IllegalArgumentException("Plugin ID cannot be empty"))
            manifest.name.isBlank() -> 
                return Result.failure(IllegalArgumentException("Plugin name cannot be empty"))
            manifest.description.isBlank() -> 
                return Result.failure(IllegalArgumentException("Plugin description cannot be empty"))
            manifest.author.name.isBlank() -> 
                return Result.failure(IllegalArgumentException("Plugin author name cannot be empty"))
            manifest.versionCode < 1 -> 
                return Result.failure(IllegalArgumentException("Plugin versionCode must be positive"))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Compare two semantic versions
     * @return negative if v1 < v2, 0 if equal, positive if v1 > v2
     */
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrNull(i) ?: 0
            val p2 = parts2.getOrNull(i) ?: 0
            
            if (p1 != p2) {
                return p1 - p2
            }
        }
        
        return 0
    }
}
