package ireader.data.catalog

import ireader.core.log.Log
import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.MangaInfo
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.ExtensionTrustLevel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtensionSecurityManagerImplTest {

    private val securityManager = ExtensionSecurityManagerImpl(Log)

    private fun createCatalog(pkgName: String, sourceId: Long = 100L): CatalogInstalled.SystemWide {
        val dummySource = object : Source {
            override val id: Long = sourceId
            override val name: String = "Test"
            override val lang: String = "en"
            override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo = manga
            override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> = emptyList()
            override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<ireader.core.source.model.Page> = emptyList()
        }
        return CatalogInstalled.SystemWide(
            name = "Test Extension",
            description = "Test description",
            source = dummySource,
            pkgName = pkgName,
            versionName = "1.0.0",
            versionCode = 1,
            nsfw = false,
            isPinned = false,
            hasUpdate = false,
            iconUrl = "",
            installDir = null
        )
    }

    @Test
    fun verifySignatureShouldReturnFalseForPackagePrefixWithoutCryptographicSignature() = runTest {
        // Attack vector: malicious APKs naming themselves with official package prefixes
        val catalogIReader = createCatalog("ireader.extension.untrusted")
        val catalogTachiyomi = createCatalog("eu.kanade.tachiyomi.extension.untrusted")
        val catalogRandom = createCatalog("com.attacker.extension")

        assertFalse(securityManager.verifySignature(catalogIReader), "Should not verify based on package name prefix")
        assertFalse(securityManager.verifySignature(catalogTachiyomi), "Should not verify based on package name prefix")
        assertFalse(securityManager.verifySignature(catalogRandom), "Should not verify arbitrary package")
    }

    @Test
    fun getTrustLevelShouldDefaultToUntrusted() = runTest {
        val catalog = createCatalog("ireader.extension.untrusted", sourceId = 101L)
        val trustLevel = securityManager.getTrustLevel(catalog)

        assertEquals(ExtensionTrustLevel.UNTRUSTED, trustLevel)
    }

    @Test
    fun getTrustLevelShouldHonorExplicitUserTrust() = runTest {
        val extensionId = 102L
        val catalog = createCatalog("ireader.extension.trusted", sourceId = extensionId)

        securityManager.setTrustLevel(extensionId, ExtensionTrustLevel.TRUSTED)
        assertEquals(ExtensionTrustLevel.TRUSTED, securityManager.getTrustLevel(catalog))
        assertTrue(securityManager.isTrusted(extensionId))
    }

    @Test
    fun getTrustLevelShouldHonorExplicitBlockedStatus() = runTest {
        val extensionId = 103L
        val catalog = createCatalog("com.attacker.extension", sourceId = extensionId)

        securityManager.setTrustLevel(extensionId, ExtensionTrustLevel.BLOCKED)
        assertEquals(ExtensionTrustLevel.BLOCKED, securityManager.getTrustLevel(catalog))
        assertFalse(securityManager.isTrusted(extensionId))
    }

    @Test
    fun getSecurityWarningsShouldWarnAboutUnverifiedSignature() = runTest {
        val catalog = createCatalog("ireader.extension.untrusted", sourceId = 104L)
        val warnings = securityManager.getSecurityWarnings(catalog)

        assertTrue(warnings.any { it.contains("signature could not be verified", ignoreCase = true) })
    }
}
