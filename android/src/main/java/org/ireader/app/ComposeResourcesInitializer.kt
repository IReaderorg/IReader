package org.ireader.app

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * ContentProvider that initializes Compose Multiplatform resources context automatically.
 * This runs before Application.onCreate() and ensures resources have access to the Android context.
 */
class ComposeResourcesInitializer : ContentProvider() {
    
    override fun onCreate(): Boolean {
        val context = context ?: return false
        
        try {
            // Try multiple approaches to initialize the context
            val appContext = context.applicationContext
            
            // Approach 1: Try AndroidContextProvider class
            try {
                val providerClass = Class.forName("org.jetbrains.compose.resources.AndroidContextProvider")
                Log.d("ComposeResourcesInit", "Found AndroidContextProvider class")
                
                // List all methods and fields for debugging
                providerClass.declaredMethods.forEach { method ->
                    Log.d("ComposeResourcesInit", "Method: ${method.name}")
                }
                providerClass.declaredFields.forEach { field ->
                    Log.d("ComposeResourcesInit", "Field: ${field.name}")
                }
                
                // Try to find the androidContext field (it's a private var)
                try {
                    val field = providerClass.getDeclaredField("androidContext")
                    field.isAccessible = true
                    field.set(null, appContext)
                    Log.d("ComposeResourcesInit", "✅ Set androidContext field successfully")
                    return true
                } catch (e: Exception) {
                    Log.e("ComposeResourcesInit", "Failed to set androidContext field", e)
                }
                
                // Try getAndroidContext and setAndroidContext
                try {
                    val setMethod = providerClass.getDeclaredMethod("setAndroidContext", Context::class.java)
                    setMethod.isAccessible = true
                    setMethod.invoke(null, appContext)
                    Log.d("ComposeResourcesInit", "✅ Called setAndroidContext method successfully")
                    return true
                } catch (e: Exception) {
                    Log.e("ComposeResourcesInit", "Failed to call setAndroidContext", e)
                }
            } catch (e: ClassNotFoundException) {
                Log.e("ComposeResourcesInit", "AndroidContextProvider class not found", e)
            }
            
            // Approach 2: Try to set via ResourceReader
            try {
                val readerClass = Class.forName("org.jetbrains.compose.resources.ResourceReader_androidKt")
                Log.d("ComposeResourcesInit", "Found ResourceReader_androidKt class")
                
                readerClass.declaredMethods.forEach { method ->
                    Log.d("ComposeResourcesInit", "ResourceReader method: ${method.name}")
                }
                readerClass.declaredFields.forEach { field ->
                    Log.d("ComposeResourcesInit", "ResourceReader field: ${field.name}")
                }
            } catch (e: ClassNotFoundException) {
                Log.e("ComposeResourcesInit", "ResourceReader_androidKt class not found", e)
            }
            
        } catch (e: Exception) {
            Log.e("ComposeResourcesInit", "Failed to initialize Compose resources", e)
        }
        
        return true // Return true anyway to not block app startup
    }

    // ContentProvider boilerplate - not used
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, 
                      selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, 
                       selectionArgs: Array<out String>?): Int = 0
}
