import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.zip.ZipFile
import kotlin.text.get

/**
 * CLI tool for validating IReader plugin packages before submission.
 * 
 * Usage:
 *   ./gradlew validatePlugin --plugin=MyPlugin
 *   
 * Or standalone:
 *   java -jar plugin-validator.jar MyPlugin.iplugin
 */
class PluginValidator {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val errors = mutableListOf<String>()
    private val warnings = mutableListOf<String>()
    
    fun validate(pluginFile: File): ValidationResult {
        errors.clear()
        warnings.clear()
        
        println("Validating plugin: ${pluginFile.name}")
        println("=" .repeat(50))
        
        // Check file exists and is readable
        if (!pluginFile.exists()) {
            errors.add("Plugin file does not exist: ${pluginFile.path}")
            return ValidationResult(false, errors, warnings)
        }
        
        if (!pluginFile.canRead()) {
            errors.add("Plugin file is not readable: ${pluginFile.path}")
            return ValidationResult(false, errors, warnings)
        }
        
        // Check file extension
        if (pluginFile.extension != "iplugin") {
            errors.add("Plugin file must have .iplugin extension")
        }
        
        // Validate as ZIP archive
        try {
            ZipFile(pluginFile).use { zip ->
                validateZipStructure(zip)
                validateManifest(zip)
                validateResources(zip)
                validateClasses(zip)
            }
        } catch (e: Exception) {
            errors.add("Failed to read plugin package: ${e.message}")
        }
        
        // Print results
        printResults()
        
        return ValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    private fun validateZipStructure(zip: ZipFile) {
        println("\n[1/4] Validating package structure...")
        
        val entries = zip.entries().toList().map { it.name }
        
        // Check for required files
        if ("plugin.json" !in entries) {
            errors.add("Missing required file: plugin.json")
        }
        
        // Check for recommended files
        if (!entries.any { it.startsWith("classes/") }) {
            warnings.add("No compiled classes found in classes/ directory")
        }
        
        if (!entries.any { it.endsWith(".png") || it.endsWith(".jpg") }) {
            warnings.add("No icon image found")
        }
        
        println("  ✓ Package structure validated")
    }
    
    private fun validateManifest(zip: ZipFile) {
        println("\n[2/4] Validating manifest...")
        
        val manifestEntry = zip.getEntry("plugin.json")
        if (manifestEntry == null) {
            errors.add("plugin.json not found in package")
            return
        }
        
        try {
            val manifestText = zip.getInputStream(manifestEntry).bufferedReader().readText()
            val manifest = json.parseToJsonElement(manifestText).jsonObject
            
            // Validate required fields
            validateRequiredField(manifest, "id")
            validateRequiredField(manifest, "name")
            validateRequiredField(manifest, "version")
            validateRequiredField(manifest, "versionCode")
            validateRequiredField(manifest, "description")
            validateRequiredField(manifest, "author")
            validateRequiredField(manifest, "type")
            validateRequiredField(manifest, "minIReaderVersion")
            validateRequiredField(manifest, "platforms")
            
            // Validate field formats
            manifest["id"]?.jsonPrimitive?.content?.let { id ->
                if (!id.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$"))) {
                    errors.add("Invalid plugin ID format: $id (must be reverse domain notation)")
                }
            }
            
            manifest["version"]?.jsonPrimitive?.content?.let { version ->
                if (!version.matches(Regex("^\\d+\\.\\d+\\.\\d+$"))) {
                    errors.add("Invalid version format: $version (must be MAJOR.MINOR.PATCH)")
                }
            }
            
            manifest["versionCode"]?.jsonPrimitive?.content?.toIntOrNull()?.let { code ->
                if (code <= 0) {
                    errors.add("Version code must be positive integer")
                }
            }
            
            manifest["type"]?.jsonPrimitive?.content?.let { type ->
                if (type !in listOf("THEME", "TRANSLATION", "TTS", "FEATURE")) {
                    errors.add("Invalid plugin type: $type")
                }
            }
            
            manifest["name"]?.jsonPrimitive?.content?.let { name ->
                if (name.length > 50) {
                    warnings.add("Plugin name is very long (${name.length} chars, recommended max 50)")
                }
            }
            
            manifest["description"]?.jsonPrimitive?.content?.let { desc ->
                if (desc.length < 20) {
                    warnings.add("Description is very short (${desc.length} chars, recommended min 20)")
                }
                if (desc.length > 500) {
                    warnings.add("Description is very long (${desc.length} chars, recommended max 500)")
                }
            }
            
            println("  ✓ Manifest validated")
            
        } catch (e: Exception) {
            errors.add("Failed to parse manifest: ${e.message}")
        }
    }
    
    private fun validateRequiredField(manifest: Map<String, Any?>, field: String) {
        if (field !in manifest) {
            errors.add("Missing required field in manifest: $field")
        }
    }
    
    private fun validateResources(zip: ZipFile) {
        println("\n[3/4] Validating resources...")
        
        val entries = zip.entries().toList()
        
        // Check icon
        val hasIcon = entries.any { 
            it.name.endsWith("icon.png") || it.name.endsWith("icon.jpg")
        }
        
        if (!hasIcon) {
            warnings.add("No icon file found (icon.png or icon.jpg)")
        }
        
        // Check for large files
        entries.forEach { entry ->
            val sizeMB = entry.size / (1024.0 * 1024.0)
            if (sizeMB > 10) {
                warnings.add("Large file detected: ${entry.name} (${String.format("%.2f", sizeMB)} MB)")
            }
        }
        
        // Check total package size
        val totalSize = entries.sumOf { it.size }
        val totalSizeMB = totalSize / (1024.0 * 1024.0)
        
        if (totalSizeMB > 50) {
            warnings.add("Large package size: ${String.format("%.2f", totalSizeMB)} MB (recommended max 50 MB)")
        }
        
        println("  ✓ Resources validated")
    }
    
    private fun validateClasses(zip: ZipFile) {
        println("\n[4/4] Validating compiled classes...")
        
        val classFiles = zip.entries().toList().filter { it.name.endsWith(".class") }
        
        if (classFiles.isEmpty()) {
            warnings.add("No compiled .class files found")
        } else {
            println("  Found ${classFiles.size} class file(s)")
        }
        
        println("  ✓ Classes validated")
    }
    
    private fun printResults() {
        println("\n" + "=".repeat(50))
        println("Validation Results:")
        println("=".repeat(50))
        
        if (errors.isEmpty() && warnings.isEmpty()) {
            println("✓ All checks passed!")
        } else {
            if (errors.isNotEmpty()) {
                println("\n❌ Errors (${errors.size}):")
                errors.forEach { println("  - $it") }
            }
            
            if (warnings.isNotEmpty()) {
                println("\n⚠️  Warnings (${warnings.size}):")
                warnings.forEach { println("  - $it") }
            }
        }
        
        println("\n" + "=".repeat(50))
        
        if (errors.isEmpty()) {
            println("✓ Plugin is valid and ready for submission!")
        } else {
            println("❌ Plugin has errors that must be fixed before submission")
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

// CLI entry point
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: plugin-validator <plugin-file.iplugin>")
        System.exit(1)
    }
    
    val pluginFile = File(args[0])
    val validator = PluginValidator()
    val result = validator.validate(pluginFile)
    
    System.exit(if (result.isValid) 0 else 1)
}
