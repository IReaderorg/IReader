package ireader.backup.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.File

class BackupSettingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        BackupSettingProcessor(environment.codeGenerator, environment.logger, environment.options)
}

/**
 * Generates `GeneratedBackupSettings`, the compile-time allow-list for portable settings.
 *
 * The processor scans Kotlin source roots supplied through the `backup.settings.sourceRoots`
 * option (configured in `domain/build.gradle.kts`). It looks for `PreferenceStore.get*`
 * getter calls with a literal key, a `const val` reference, or a named `key = ...` argument,
 * including multiline/object-backed calls. Generated entries are merged with the small legacy
 * mapping in `SettingsBackupRegistry`, then sorted and de-duplicated by preference key.
 *
 * New ordinary preference getters need no annotation and no registry edit. `@NoBackup` opts a
 * getter or an entire preference class out; `@BackupSetting` only overrides the inferred type or
 * stable backup key. Keys whose names indicate secrets (API keys, tokens, passwords, cookies,
 * sessions, credentials, user identifiers, PINs) or transient state (timestamps, onboarding,
 * install flags, update checks, runtime error counters) are skipped. Dynamic keys containing
 * `$`, such as per-plugin/per-source keys, are skipped because they are not portable.
 *
 * KSP cannot inspect function bodies, so this processor intentionally scans source text rather
 * than only symbol declarations. Per-file constants, qualified `Owner.CONST` references and
 * globally unique constants are resolved separately to avoid collisions between files.
 */
class BackupSettingProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()
        generated = true

        val annotated = resolver
            .getSymbolsWithAnnotation(ANNOTATION_NAME)
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull(::explicitEntry)
            .toList()

        val noBackupSymbols = resolver.getSymbolsWithAnnotation(NO_BACKUP_NAME).toList()
        val noBackupFunctions = noBackupSymbols
            .filterIsInstance<KSFunctionDeclaration>()
            .mapTo(mutableSetOf()) { it.simpleName.asString() }
        val noBackupClasses = noBackupSymbols
            .filterIsInstance<KSClassDeclaration>()
            .mapTo(mutableSetOf()) { it.simpleName.asString() }

        val sourceRoots = options[SOURCE_ROOTS_OPTION]
            ?.split(File.pathSeparator)
            ?.filter { it.isNotBlank() }
            ?.map(::File)
            ?: emptyList()

        val sourceFiles = buildList {
            addAll(sourceRoots.flatMap { root ->
                if (!root.exists()) {
                    logger.warn("Settings source root does not exist: ${root.absolutePath}")
                    emptyList()
                } else {
                    root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
                }
            })
            if (sourceRoots.isEmpty()) {
                addAll(resolver.getAllFiles().map { File(it.filePath) })
            }
        }

        val sources = sourceFiles.mapNotNull { file ->
            val text = runCatching { file.readText() }.getOrElse {
                logger.warn("Cannot read source file for settings scan: ${file.absolutePath}")
                null
            } ?: return@mapNotNull null
            file to text
        }

        val localConstants = sources.associate { (_, source) ->
            source to buildMap {
                CONST_REGEX.findAll(source).forEach { match ->
                    put(match.groupValues[1], match.groupValues[2])
                }
            }
        }

        val qualifiedConstants = buildMap {
            sources.forEach { (_, source) ->
                CONST_REGEX.findAll(source).forEach { match ->
                    val owner = DECLARATION_REGEX.findAll(source.take(match.range.first))
                        .lastOrNull()?.groupValues?.get(1)
                    if (owner != null) put("$owner.${match.groupValues[1]}", match.groupValues[2])
                }
            }
        }

        val uniqueConstants = localConstants.values
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .filterValues { it.distinct().size == 1 }
            .mapValues { it.value.first() }

        val discovered = buildList {
            sources.forEach { (_, source) ->
                val resolveConstant: (String) -> String? = { token ->
                    val locals = localConstants.getValue(source)
                    locals[token]
                        ?: qualifiedConstants[token]
                        ?: locals[token.substringAfterLast('.')]
                        ?: uniqueConstants[token.substringAfterLast('.')]
                }
                scanSource(source, resolveConstant, noBackupFunctions, noBackupClasses)
                    .forEach { add(it) }
            }
        }

        val entries = (annotated + discovered)
            .distinctBy { it.preferenceKey }
            .sortedBy { it.backupKey }

        val content = buildString {
            appendLine("// AUTO-GENERATED by backup-ksp. Do not edit manually.")
            appendLine("package $PACKAGE_NAME")
            appendLine()
            appendLine("internal object GeneratedBackupSettings {")
            appendLine("    val entries: List<SettingSpec> = listOf(")
            entries.forEach { (backupKey, key, type) ->
                appendLine(
                    "        SettingSpec(\"${escape(backupKey)}\", \"${escape(key)}\", SettingType.$type),",
                )
            }
            appendLine("    )")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = PACKAGE_NAME,
            fileName = "GeneratedBackupSettings",
        ).use { it.write(content.toByteArray()) }

        logger.info("[backup-ksp] generated ${entries.size} portable settings")
        return emptyList()
    }

    private fun explicitEntry(function: KSFunctionDeclaration): GeneratedSetting? {
        val annotation = function.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION_NAME
        }
        val key = annotation.stringArgument("key")
        if (key.isBlank()) {
            logger.warn("@BackupSetting on ${function.qualifiedName?.asString()} has an empty key", function)
            return null
        }
        val backupKey = annotation.stringArgument("backupKey").ifBlank { key }
        val type = annotation.stringArgument("type").uppercase()
        if (type !in VALID_TYPES) {
            logger.error("Invalid @BackupSetting type '$type' on ${function.qualifiedName?.asString()}", function)
            return null
        }
        return GeneratedSetting(backupKey, key, type)
    }

    private fun scanSource(
        source: String,
        resolveConstant: (String) -> String?,
        noBackupFunctions: Set<String>,
        noBackupClasses: Set<String>,
    ): List<GeneratedSetting> {
        val scannable = maskExcluded(source, noBackupFunctions, noBackupClasses)
        return GETTER_REGEX.findAll(scannable).mapNotNull { match ->
            val type = typeFor(match.groupValues[1]) ?: return@mapNotNull null
            val literalKey = match.groupValues[2].takeIf { it.isNotEmpty() }
            val constName = match.groupValues[3].takeIf { it.isNotEmpty() }
            val key = literalKey ?: constName?.let(resolveConstant) ?: return@mapNotNull null
            if (key.isBlank() || key.contains('$') || isExcluded(key)) return@mapNotNull null
            GeneratedSetting(key, key, type)
        }.toList()
    }

    /** Blanks out `@NoBackup` declarations so their getter calls are never generated. */
    private fun maskExcluded(
        source: String,
        noBackupFunctions: Set<String>,
        noBackupClasses: Set<String>,
    ): String {
        var masked = source
        val nextDeclaration = Regex("""\bfun\s+\w+\s*\(|\b(?:class|object)\s+\w+""")

        noBackupFunctions.forEach { name ->
            val match = Regex("""\bfun\s+${Regex.escape(name)}\s*\(""").find(masked) ?: return@forEach
            val end = nextDeclaration.find(masked, match.range.last + 1)?.range?.first ?: masked.length
            val start = match.range.first
            masked = masked.replaceRange(start until end, " ".repeat(end - start))
        }
        noBackupClasses.forEach { name ->
            val match = Regex("""\b(?:class|object)\s+${Regex.escape(name)}\b""").find(masked) ?: return@forEach
            val end = nextDeclaration.find(masked, match.range.last + 1)?.range?.first ?: masked.length
            val start = match.range.first
            masked = masked.replaceRange(start until end, " ".repeat(end - start))
        }
        return masked
    }

    private fun typeFor(method: String): String? = when (method) {
        "getInt" -> "INT"
        "getLong" -> "LONG"
        "getFloat" -> "FLOAT"
        "getBoolean" -> "BOOLEAN"
        "getString" -> "STRING"
        "getStringSet" -> "STRING_SET"
        "getEnum", "getObject", "getJsonObject" -> "STRING"
        else -> null
    }

    private fun isExcluded(key: String): Boolean {
        val lower = key.lowercase()
        return SENSITIVE_REGEX.containsMatchIn(lower) || TRANSIENT_REGEX.containsMatchIn(lower)
    }

    private fun com.google.devtools.ksp.symbol.KSAnnotation.stringArgument(name: String): String =
        arguments.firstOrNull { it.name?.asString() == name }?.value as? String ?: ""

    private fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")

    private data class GeneratedSetting(
        val backupKey: String,
        val preferenceKey: String,
        val type: String,
    )

    private companion object {
        const val PACKAGE_NAME = "ireader.domain.usecases.backup.v2"
        const val ANNOTATION_NAME = "$PACKAGE_NAME.BackupSetting"
        const val NO_BACKUP_NAME = "$PACKAGE_NAME.NoBackup"
        const val SOURCE_ROOTS_OPTION = "backup.settings.sourceRoots"

        val VALID_TYPES = setOf("INT", "LONG", "FLOAT", "BOOLEAN", "STRING", "STRING_SET")

        val CONST_REGEX = Regex("""\bconst\s+val\s+(\w+)\s*(?::\s*String\s*)?=\s*"([^"]+)\"""")
        val DECLARATION_REGEX = Regex("""\b(?:class|object)\s+(\w+)""")
        val GETTER_REGEX = Regex(
            """\b(?:preferenceStore|store|prefs|preferences|settings)\s*\.\s*(getStringSet|getBoolean|getInt|getLong|getFloat|getEnum|getObject|getJsonObject|getString)\s*\(\s*(?:key\s*=\s*)?(?:"([^"]+)"|([A-Za-z_][A-Za-z0-9_.]*))\s*[,)]""",
        )

        val SENSITIVE_REGEX = Regex(
            "(^|[_-])(api[_-]?keys?|secrets?|tokens?|passwords?|passwds?|passcodes?|cookies?|sessions?|auths?|credentials?|private[_-]?keys?|access[_-]?keys?|refreshes?|encryptions?|master[_-]?keys?|pins?|user[_-](ids?|names?))([_-]|$)",
        )
        val TRANSIENT_REGEX = Regex(
            "(timestamp|onboard|first[_-]?run|install[_-]|consent[_-]?completed|^last[_-].*time|app[_-](unlock|closed|open)|update[_-]check|source[_-]errors|database[_-]version)",
        )
    }
}
