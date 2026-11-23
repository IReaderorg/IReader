package ireader.domain.usecases.backup.v2

/**
 * Overrides backup metadata inferred from a preference getter.
 *
 * Ordinary `PreferenceStore.getX(...)` getters are discovered automatically by the KSP processor,
 * so this annotation is only needed when the inferred type or stable backup key must be changed
 * (for example, an object-backed setting that should be treated as a string).
 *
 * Secrets, session state, and transient timestamps are skipped by key-name rules. Annotate a
 * getter or preference class with [NoBackup] to opt out explicitly.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class BackupSetting(
    /** The actual DataStore preference key used by the getter. */
    val key: String,
    /** Wire type name: INT, LONG, FLOAT, BOOLEAN, STRING or STRING_SET. */
    val type: String,
    /** Override the backup key (used for legacy key mappings). Defaults to [key]. */
    val backupKey: String = "",
)

/** Explicitly excludes a preference accessor from automatic backup registration. */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NoBackup
