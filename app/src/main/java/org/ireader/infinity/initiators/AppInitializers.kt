package org.ireader.infinity.initiators

import javax.inject.Inject

class AppInitializers @Inject constructor(
    emojiCompatInitializer: EmojiCompatInitializer,
    notificationsInitializer: NotificationsInitializer,
    crashHandler: CrashHandler,
    firebaseInitializer: FirebaseInitializer,
    updateServiceInitializer: UpdateServiceInitializer,
    catalogStoreInitializer: CatalogStoreInitializer,
)
