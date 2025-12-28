package ireader.presentation.ui.settings.data

import android.content.Context
import ireader.core.log.Log
import java.io.File

/**
 * Android-specific implementation of DatabaseHelper
 */
class AndroidDatabaseHelper(private val context: Context) : DatabaseHelper {
    
    override suspend fun optimizeDatabase(): Boolean {
        return try {
            // Find the database file
            val dbFile = context.getDatabasePath("ireader.db")
            if (!dbFile.exists()) {
                Log.warn { "Database file not found" }
                return false
            }
            
            // Open database and run VACUUM
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            )
            
            try {
                // Run VACUUM to reclaim space and defragment
                db.execSQL("VACUUM")
                
                // Run ANALYZE to update statistics
                db.execSQL("ANALYZE")
                
                // Run integrity check
                val cursor = db.rawQuery("PRAGMA integrity_check", null)
                val isHealthy = cursor.use {
                    it.moveToFirst() && it.getString(0) == "ok"
                }
                
                if (!isHealthy) {
                    Log.warn { "Database integrity check failed" }
                }
                
                Log.info { "Database optimization completed successfully" }
                true
            } finally {
                db.close()
            }
        } catch (e: Exception) {
            Log.error(e, "Database optimization failed")
            false
        }
    }
}
