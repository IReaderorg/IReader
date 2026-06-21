package ireader.domain.services.sync

/**
 * Desktop-specific test factory for KeyStorageService.
 */
actual fun createKeyStorageService(): KeyStorageService {
    return InMemoryKeyStorageService()
}

/**
 * Simple in-memory implementation for testing on desktop.
 */
class InMemoryKeyStorageService : KeyStorageService {
    private val store = mutableMapOf<String, ByteArray>()

    override suspend fun storeKey(alias: String, key: ByteArray): Result<Unit> {
        store[alias] = key.copyOf()
        return Result.success(Unit)
    }

    override suspend fun retrieveKey(alias: String): Result<ByteArray> {
        val key = store[alias] ?: return Result.failure(Exception("Key not found: $alias"))
        return Result.success(key.copyOf())
    }

    override suspend fun deleteKey(alias: String): Result<Unit> {
        store.remove(alias)
        return Result.success(Unit)
    }

    override suspend fun keyExists(alias: String): Boolean {
        return store.containsKey(alias)
    }

    override suspend fun listKeys(): List<String> {
        return store.keys.toList()
    }
}
