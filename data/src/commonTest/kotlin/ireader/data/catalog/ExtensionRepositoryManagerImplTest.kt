package ireader.data.catalog

import ireader.core.log.Log
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import ireader.domain.models.entities.ExtensionTrustLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtensionRepositoryManagerImplTest {

    private class FakeCatalogSourceRepository : CatalogSourceRepository {
        val sources = mutableListOf<ExtensionSource>()
        var nextId = 1L

        override fun subscribe(): Flow<List<ExtensionSource>> = flowOf(sources.toList())
        override suspend fun find(id: Long): ExtensionSource? = sources.find { it.id == id }
        override suspend fun insert(extensionSource: ExtensionSource) {
            val inserted = extensionSource.copy(id = nextId++)
            sources.add(inserted)
        }
        override suspend fun update(extensionSource: ExtensionSource) {
            val index = sources.indexOfFirst { it.id == extensionSource.id }
            if (index != -1) sources[index] = extensionSource
        }
        override suspend fun delete(extensionSource: ExtensionSource) {
            sources.removeAll { it.id == extensionSource.id }
        }
    }

    private val fakeRepo = FakeCatalogSourceRepository()
    private val repositoryManager = ExtensionRepositoryManagerImpl(fakeRepo, Log)

    @Test
    fun addRepositoryWithArbitraryGithubUrlShouldBeUntrusted() = runTest {
        // Attack vector: Malicious repo containing "ireader-extensions" in its name
        val result = repositoryManager.addRepository(
            name = "Malicious Repo",
            url = "https://github.com/attacker/my-ireader-extensions",
            fingerprint = null
        )

        assertTrue(result.isSuccess)
        val repo = result.getOrThrow()
        assertEquals(
            ExtensionTrustLevel.UNTRUSTED,
            repo.trustLevel,
            "Arbitrary GitHub repo containing ireader-extensions must not be TRUSTED or VERIFIED"
        )
    }

    @Test
    fun addRepositoryWithArbitrary64CharFingerprintShouldBeUntrusted() = runTest {
        // Attack vector: Attacker supplying an arbitrary 64-char hex string as fingerprint
        val random64HexFingerprint = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        val result = repositoryManager.addRepository(
            name = "Fake Verified Repo",
            url = "https://github.com/untrusted-user/plugins",
            fingerprint = random64HexFingerprint
        )

        assertTrue(result.isSuccess)
        val repo = result.getOrThrow()
        assertEquals(
            ExtensionTrustLevel.UNTRUSTED,
            repo.trustLevel,
            "Arbitrary 64-char hex fingerprint must not bypass verification"
        )
    }

    @Test
    fun addRepositoryWithOfficialUrlShouldBeTrusted() = runTest {
        val result = repositoryManager.addRepository(
            name = "Official IReader Extensions",
            url = "https://raw.githubusercontent.com/ireaderorg/ireader-extensions/main/index.json",
            fingerprint = null
        )

        assertTrue(result.isSuccess)
        val repo = result.getOrThrow()
        assertEquals(
            ExtensionTrustLevel.TRUSTED,
            repo.trustLevel,
            "Official IReader extension repository must be TRUSTED"
        )
    }
}
