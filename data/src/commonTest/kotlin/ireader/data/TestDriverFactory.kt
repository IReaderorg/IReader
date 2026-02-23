package ireader.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific test driver factory for SQLDelight database testing.
 * Each platform provides its own implementation.
 */
expect fun createTestDriver(): SqlDriver
