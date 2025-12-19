package ireader.domain.js.converter

/**
 * Simple JavaScript AST parser for extracting plugin metadata.
 * Focuses on object literals and function declarations without full JS parsing.
 */
class JSASTParser {
    
    data class ObjectLiteral(
        val properties: Map<String, Any>,
        val startIndex: Int,
        val endIndex: Int
    )
    
    data class FunctionDeclaration(
        val name: String,
        val params: List<String>,
        val body: String,
        val isAsync: Boolean,
        val startIndex: Int,
        val endIndex: Int
    )
    
    data class ParsedPlugin(
        val metadata: ObjectLiteral?,
        val functions: Map<String, FunctionDeclaration>,
        val classProperties: Map<String, Any>
    )
    
    /**
     * Parse JavaScript code to extract structured data.
     */
    fun parse(jsCode: String): ParsedPlugin {
        println("JSASTParser.parse: Starting parse of ${jsCode.length} chars")
        
        val cleanCode = removeComments(jsCode)
        println("JSASTParser.parse: After removing comments: ${cleanCode.length} chars")
        
        val metadata = extractMetadataObject(cleanCode)
        println("JSASTParser.parse: Metadata extracted: ${metadata != null}")
        
        val functions = extractFunctions(cleanCode)
        println("JSASTParser.parse: Functions extracted: ${functions.size}")
        
        val classProperties = extractClassProperties(cleanCode)
        println("JSASTParser.parse: Class properties extracted: ${classProperties.size}")
        
        return ParsedPlugin(metadata, functions, classProperties)
    }
    
    /**
     * Remove single-line and multi-line comments from JavaScript code.
     * Properly handles strings and template literals to avoid removing
     * comment-like content inside strings.
     */
    private fun removeComments(code: String): String {
        val result = StringBuilder()
        var i = 0
        val len = code.length
        
        while (i < len) {
            val c = code[i]
            
            // Handle strings (single quote, double quote, backtick)
            if (c == '"' || c == '\'' || c == '`') {
                val quote = c
                result.append(c)
                i++
                
                // Copy string content until closing quote
                while (i < len) {
                    val sc = code[i]
                    result.append(sc)
                    
                    if (sc == '\\' && i + 1 < len) {
                        // Escape sequence - copy next char too
                        i++
                        result.append(code[i])
                    } else if (sc == quote) {
                        // End of string (for backticks, handle ${} separately if needed)
                        break
                    }
                    i++
                }
                i++
                continue
            }
            
            // Handle single-line comment //
            if (c == '/' && i + 1 < len && code[i + 1] == '/') {
                // Skip until end of line
                while (i < len && code[i] != '\n') {
                    i++
                }
                // Keep the newline to preserve line numbers
                if (i < len) {
                    result.append('\n')
                    i++
                }
                continue
            }
            
            // Handle multi-line comment /* */
            if (c == '/' && i + 1 < len && code[i + 1] == '*') {
                i += 2 // Skip /*
                // Find closing */
                while (i + 1 < len) {
                    if (code[i] == '*' && code[i + 1] == '/') {
                        i += 2 // Skip */
                        break
                    }
                    // Preserve newlines to maintain line numbers
                    if (code[i] == '\n') {
                        result.append('\n')
                    }
                    i++
                }
                continue
            }
            
            // Handle regex literals (to avoid treating /.../ as division or comment)
            if (c == '/') {
                // Check if this could be a regex (simplified heuristic)
                // Regex typically follows: (, [, {, =, :, ;, ,, !, &, |, ^, ~, return, etc.
                val prevNonSpace = result.toString().trimEnd().lastOrNull()
                val isRegexContext = prevNonSpace == null || 
                    prevNonSpace in "([{=:;,!&|^~?" ||
                    result.toString().trimEnd().endsWith("return")
                
                if (isRegexContext && i + 1 < len && code[i + 1] != '/' && code[i + 1] != '*') {
                    // Likely a regex - copy until closing /
                    result.append(c)
                    i++
                    var inCharClass = false
                    while (i < len) {
                        val rc = code[i]
                        result.append(rc)
                        
                        if (rc == '\\' && i + 1 < len) {
                            // Escape - copy next char
                            i++
                            result.append(code[i])
                        } else if (rc == '[') {
                            inCharClass = true
                        } else if (rc == ']') {
                            inCharClass = false
                        } else if (rc == '/' && !inCharClass) {
                            // End of regex
                            break
                        }
                        i++
                    }
                    i++
                    // Copy regex flags
                    while (i < len && code[i].isLetter()) {
                        result.append(code[i])
                        i++
                    }
                    continue
                }
            }
            
            // Regular character
            result.append(c)
            i++
        }
        
        return result.toString()
    }
    
    /**
     * Extract the main metadata object from the plugin.
     * Looks for patterns like: { id: "...", name: "...", ... }
     */
    private fun extractMetadataObject(code: String): ObjectLiteral? {
        println("JSASTParser.extractMetadataObject: Searching in ${code.length} chars")
        
        // First try to find "new PluginClass({ ... })" pattern with proper brace matching
        val newPattern = """new\s+\w+\s*\(\s*\{""".toRegex()
        val newMatch = newPattern.find(code)
        println("JSASTParser.extractMetadataObject: 'new Plugin({' pattern found: ${newMatch != null}")
        
        if (newMatch != null) {
            val startIndex = newMatch.range.last - 1 // Position of opening {
            println("JSASTParser: startIndex = $startIndex, looking for matching brace...")
            
            val endIndex = findMatchingBrace(code, startIndex + 1)
            println("JSASTParser: endIndex = $endIndex")
            
            if (endIndex > startIndex) {
                val objectText = code.substring(startIndex, endIndex + 1)
                println("JSASTParser: Found 'new Plugin()' pattern (${objectText.length} chars)")
                println("JSASTParser: First 200 chars: ${objectText.take(200)}")
                
                try {
                    val properties = parseObjectLiteral(objectText)
                    println("JSASTParser: Parsed ${properties.size} properties: ${properties.keys}")
                    
                    if (properties.containsKey("id") || properties.containsKey("name") || 
                        properties.containsKey("sourceName") || properties.containsKey("sourceSite")) {
                        println("JSASTParser: Found valid metadata object!")
                        return ObjectLiteral(
                            properties = properties,
                            startIndex = startIndex,
                            endIndex = endIndex
                        )
                    } else {
                        println("JSASTParser: Object doesn't contain expected keys")
                    }
                } catch (e: Exception) {
                    println("JSASTParser: Error parsing object: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                println("JSASTParser: Could not find matching brace (endIndex <= startIndex)")
            }
        }
        
        // Fallback: Try to extract just the essential fields using regex
        // This works better for complex TypeScript-compiled plugins
        val simpleExtraction = extractSimpleMetadata(code)
        if (simpleExtraction != null) {
            println("JSASTParser: Using simple regex extraction as fallback")
            return simpleExtraction
        }
        
        // Fallback to other patterns
        val patterns = listOf(
            // Pattern 1: Direct object export: export default { ... }
            """export\s+default\s+(\{[\s\S]+?\n\})""".toRegex(),
            // Pattern 2: Object literal at start
            """^\s*(\{[\s\S]{100,2000}?\})""".toRegex(RegexOption.MULTILINE),
            // Pattern 3: Minified object with id/name
            """(\{[^{}]{50,500}?["']?id["']?\s*:\s*['"][^'"]+['"][^{}]{50,500}?\})""".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(code)
            if (match != null) {
                val objectText = match.groupValues[1]
                println("JSASTParser: Found potential metadata object (${objectText.length} chars)")
                println("JSASTParser: First 200 chars: ${objectText.take(200)}")
                
                try {
                    val properties = parseObjectLiteral(objectText)
                    println("JSASTParser: Parsed ${properties.size} properties: ${properties.keys}")
                    
                    if (properties.containsKey("id") || properties.containsKey("name") || 
                        properties.containsKey("sourceName") || properties.containsKey("sourceSite")) {
                        return ObjectLiteral(
                            properties = properties,
                            startIndex = match.range.first,
                            endIndex = match.range.last
                        )
                    }
                } catch (e: Exception) {
                    println("JSASTParser: Error parsing object: ${e.message}")
                }
            }
        }
        
        return null
    }
    
    /**
     * Parse an object literal string into a map of properties.
     * Handles nested objects and arrays to one level.
     */
    private fun parseObjectLiteral(objectText: String): Map<String, Any> {
        val properties = mutableMapOf<String, Any>()
        
        // Remove outer braces
        val content = objectText.trim().removeSurrounding("{", "}")
        
        // Split by commas, but respect nested structures
        val propertyStrings = splitByComma(content)
        
        for (propStr in propertyStrings) {
            val colonIndex = propStr.indexOf(':')
            if (colonIndex == -1) continue
            
            var key = propStr.substring(0, colonIndex).trim()
            
            // Remove quotes from key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length - 1)
            } else if (key.startsWith("'") && key.endsWith("'")) {
                key = key.substring(1, key.length - 1)
            }
            
            if (key.isBlank()) continue
            
            val valueStr = propStr.substring(colonIndex + 1).trim()
            val value = parseValue(valueStr)
            
            properties[key] = value
        }
        
        return properties
    }
    
    /**
     * Split a string by commas, respecting nested braces and brackets.
     */
    private fun splitByComma(text: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var braceDepth = 0
        var bracketDepth = 0
        var inString = false
        var stringChar = ' '
        var i = 0
        
        while (i < text.length) {
            val char = text[i]
            
            when {
                (char == '"' || char == '\'') && (i == 0 || text[i - 1] != '\\') -> {
                    if (!inString) {
                        inString = true
                        stringChar = char
                    } else if (char == stringChar) {
                        inString = false
                    }
                    current.append(char)
                }
                inString -> {
                    current.append(char)
                }
                char == '{' -> {
                    braceDepth++
                    current.append(char)
                }
                char == '}' -> {
                    braceDepth--
                    current.append(char)
                }
                char == '[' -> {
                    bracketDepth++
                    current.append(char)
                }
                char == ']' -> {
                    bracketDepth--
                    current.append(char)
                }
                char == ',' && braceDepth == 0 && bracketDepth == 0 -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString().trim())
                        current = StringBuilder()
                    }
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        
        if (current.isNotEmpty()) {
            result.add(current.toString().trim())
        }
        
        return result
    }
    
    /**
     * Parse a JavaScript value (string, number, boolean, object, array, function).
     */
    private fun parseValue(valueStr: String): Any {
        val trimmed = valueStr.trim()
        
        return when {
            // String literal
            trimmed.startsWith("\"") || trimmed.startsWith("'") -> {
                trimmed.removeSurrounding("\"", "\"")
                    .removeSurrounding("'", "'")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
            }
            // Template literal
            trimmed.startsWith("`") -> {
                trimmed.removeSurrounding("`", "`")
            }
            // Boolean
            trimmed == "true" -> true
            trimmed == "false" -> false
            // Null/undefined
            trimmed == "null" || trimmed == "undefined" -> ""
            // Number
            trimmed.toIntOrNull() != null -> trimmed.toInt()
            trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
            // Array
            trimmed.startsWith("[") -> {
                val arrayContent = trimmed.removeSurrounding("[", "]")
                splitByComma(arrayContent).map { parseValue(it) }
            }
            // Object
            trimmed.startsWith("{") -> {
                parseObjectLiteral(trimmed)
            }
            // Function (arrow or regular)
            trimmed.contains("=>") || trimmed.startsWith("function") -> {
                "function" // Placeholder for functions
            }
            // Default: return as string
            else -> trimmed
        }
    }
    
    /**
     * Extract all function declarations from the code.
     */
    private fun extractFunctions(code: String): Map<String, FunctionDeclaration> {
        val functions = mutableMapOf<String, FunctionDeclaration>()
        
        // Pattern for various function declarations
        val patterns = listOf(
            // async function name(params) { body }
            """(async\s+)?function\s+(\w+)\s*\(([^)]*)\)\s*\{""".toRegex(),
            // name: async function(params) { body }
            """(\w+)\s*:\s*(async\s+)?function\s*\(([^)]*)\)\s*\{""".toRegex(),
            // async name(params) { body } (method shorthand)
            """(async\s+)?(\w+)\s*\(([^)]*)\)\s*\{""".toRegex(),
            // name: async (params) => { body }
            """(\w+)\s*:\s*(async\s+)?\(([^)]*)\)\s*=>\s*\{""".toRegex()
        )
        
        for (pattern in patterns) {
            pattern.findAll(code).forEach { match ->
                val isAsync = match.groupValues.any { it.trim() == "async" }
                val nameIndex = match.groupValues.indexOfFirst { 
                    it.isNotBlank() && it != "async" && !it.contains("(") 
                }
                
                if (nameIndex > 0) {
                    val name = match.groupValues[nameIndex]
                    val paramsIndex = match.groupValues.indexOfLast { it.contains(",") || it.isBlank() }
                    val params = if (paramsIndex > 0) {
                        match.groupValues[paramsIndex]
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }
                    
                    // Extract function body
                    val bodyStart = match.range.last + 1
                    val bodyEnd = findMatchingBrace(code, bodyStart)
                    val body = if (bodyEnd > bodyStart) {
                        code.substring(bodyStart, bodyEnd)
                    } else {
                        ""
                    }
                    
                    functions[name] = FunctionDeclaration(
                        name = name,
                        params = params,
                        body = body,
                        isAsync = isAsync,
                        startIndex = match.range.first,
                        endIndex = bodyEnd
                    )
                }
            }
        }
        
        return functions
    }
    
    /**
     * Find the matching closing brace for an opening brace.
     */
    private fun findMatchingBrace(code: String, startIndex: Int): Int {
        println("JSASTParser.findMatchingBrace: Starting at $startIndex, code length = ${code.length}")
        
        var depth = 1
        var inString = false
        var stringChar = ' '
        var i = startIndex
        var lastDepthChange = startIndex
        
        while (i < code.length && depth > 0) {
            val char = code[i]
            
            when {
                (char == '"' || char == '\'') && !isEscaped(code, i) -> {
                    if (!inString) {
                        inString = true
                        stringChar = char
                    } else if (char == stringChar) {
                        inString = false
                    }
                }
                !inString && char == '{' -> {
                    depth++
                    lastDepthChange = i
                }
                !inString && char == '}' -> {
                    depth--
                    lastDepthChange = i
                    if (depth == 0) {
                        println("JSASTParser.findMatchingBrace: Found matching brace at $i")
                    }
                }
            }
            
            i++
        }
        
        println("JSASTParser.findMatchingBrace: Finished at $i, depth = $depth, lastDepthChange = $lastDepthChange")
        if (depth > 0) {
            println("JSASTParser.findMatchingBrace: Missing $depth closing brace(s)")
            // Show context around last depth change
            val contextStart = maxOf(0, lastDepthChange - 50)
            val contextEnd = minOf(code.length, lastDepthChange + 50)
            println("JSASTParser.findMatchingBrace: Context: ${code.substring(contextStart, contextEnd)}")
        }
        
        return if (depth == 0) i - 1 else -1
    }
    
    /**
     * Check if a character at position i is escaped by counting preceding backslashes.
     */
    private fun isEscaped(code: String, i: Int): Boolean {
        if (i == 0) return false
        var backslashCount = 0
        var j = i - 1
        while (j >= 0 && code[j] == '\\') {
            backslashCount++
            j--
        }
        // Odd number of backslashes means the character is escaped
        return backslashCount % 2 == 1
    }
    
    /**
     * Simple metadata extraction using regex for TypeScript-compiled plugins.
     * Extracts just the essential fields from new Plugin({ ... }) calls.
     */
    private fun extractSimpleMetadata(code: String): ObjectLiteral? {
        // Find the new Plugin({ ... }) pattern
        val newPattern = """new\s+\w+\s*\(\s*\{""".toRegex()
        val match = newPattern.find(code) ?: return null
        
        // Extract a reasonable chunk after the opening brace (first 5000 chars should contain metadata)
        val startPos = match.range.last
        val endPos = minOf(code.length, startPos + 5000)
        val chunk = code.substring(startPos, endPos)
        
        // Extract individual fields using regex
        val properties = mutableMapOf<String, Any>()
        
        // Extract quoted string fields
        val fieldPatterns = listOf(
            """"id"\s*:\s*"([^"]+)"""" to "id",
            """"sourceName"\s*:\s*"([^"]+)"""" to "sourceName",
            """"sourceSite"\s*:\s*"([^"]+)"""" to "sourceSite",
            """"name"\s*:\s*"([^"]+)"""" to "name",
            """"site"\s*:\s*"([^"]+)"""" to "site",
            """"version"\s*:\s*"([^"]+)"""" to "version",
            """"lang"\s*:\s*"([^"]+)"""" to "lang",
            """"icon"\s*:\s*"([^"]+)"""" to "icon"
        )
        
        for ((pattern, key) in fieldPatterns) {
            val regex = pattern.toRegex()
            val fieldMatch = regex.find(chunk)
            if (fieldMatch != null) {
                properties[key] = fieldMatch.groupValues[1]
                println("JSASTParser.extractSimpleMetadata: Found $key = ${fieldMatch.groupValues[1]}")
            }
        }
        
        return if (properties.isNotEmpty()) {
            ObjectLiteral(
                properties = properties,
                startIndex = startPos,
                endIndex = startPos + chunk.length
            )
        } else {
            null
        }
    }
    
    /**
     * Extract class properties (for class-based plugins).
     */
    private fun extractClassProperties(code: String): Map<String, Any> {
        val properties = mutableMapOf<String, Any>()
        
        // Find class declaration
        val classPattern = """class\s+(\w+)[\s\S]*?\{([\s\S]+?)\}""".toRegex()
        val classMatch = classPattern.find(code) ?: return properties
        
        val classBody = classMatch.groupValues[2]
        
        // Extract property assignments: this.property = value
        val propertyPattern = """this\.(\w+)\s*=\s*([^;]+);""".toRegex()
        propertyPattern.findAll(classBody).forEach { match ->
            val name = match.groupValues[1]
            val value = parseValue(match.groupValues[2])
            properties[name] = value
        }
        
        // Extract getter methods: get property() { return value; }
        val getterPattern = """get\s+(\w+)\s*\(\s*\)\s*\{\s*return\s+([^;]+);""".toRegex()
        getterPattern.findAll(classBody).forEach { match ->
            val name = match.groupValues[1]
            val value = parseValue(match.groupValues[2])
            properties[name] = value
        }
        
        return properties
    }
    
    /**
     * Helper to safely get string value from parsed data.
     */
    fun getStringValue(data: Any?): String {
        return when (data) {
            is String -> data
            is Map<*, *> -> data["value"]?.toString() ?: ""
            else -> data?.toString() ?: ""
        }
    }
    
    /**
     * Helper to safely get nested property from object.
     */
    fun getNestedProperty(obj: Map<String, Any>, path: String): Any? {
        val parts = path.split(".")
        var current: Any? = obj
        
        for (part in parts) {
            current = when (current) {
                is Map<*, *> -> current[part]
                else -> null
            }
            if (current == null) break
        }
        
        return current
    }
}
