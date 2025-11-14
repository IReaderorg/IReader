package org.ireader.app.initiators

/**
 * FDroid variant - no Firebase dependencies
 */
class AppInitializers(
    private val emojiCompatInitializer: EmojiCompatInitializer,
    private val notificationsInitializer: NotificationsInitializer,
    private val crashHandler: CrashHandler,
    private val firebaseInitializer: FirebaseInitializer,
    private val updateServiceInitializer: UpdateServiceInitializer,
    private val catalogStoreInitializer: CatalogStoreInitializer,
) {
    fun init() {
        emojiCompatInitializer
        notificationsInitializer
        crashHandler
        firebaseInitializer
        updateServiceInitializer
        catalogStoreInitializer
    }
}
