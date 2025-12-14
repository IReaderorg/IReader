package ireader.data.usersource

import data.User_source
import ireader.data.core.DatabaseHandler
import ireader.domain.usersource.model.*
import ireader.domain.usersource.repository.UserSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * SQLDelight-backed implementation of UserSourceRepository.
 */
class UserSourceRepositoryImpl(
    private val handler: DatabaseHandler
) : UserSourceRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    override fun getAllAsFlow(): Flow<List<UserSource>> {
        return handler.subscribeToList { userSourceQueries.findAll() }
            .map { list -> list.map { it.toUserSource() } }
    }
    
    override suspend fun getAll(): List<UserSource> {
        return handler.awaitList { userSourceQueries.findAll() }
            .map { it.toUserSource() }
    }
    
    override suspend fun getEnabled(): List<UserSource> {
        return handler.awaitList { userSourceQueries.findEnabled() }
            .map { it.toUserSource() }
    }
    
    override suspend fun getByUrl(sourceUrl: String): UserSource? {
        return handler.awaitOneOrNull { userSourceQueries.findByUrl(sourceUrl) }
            ?.toUserSource()
    }
    
    override suspend fun getById(sourceId: Long): UserSource? {
        return handler.awaitOneOrNull { userSourceQueries.findById(sourceId) }
            ?.toUserSource()
    }
    
    override suspend fun upsert(source: UserSource) {
        handler.await {
            userSourceQueries.insert(
                source_url = source.sourceUrl,
                source_name = source.sourceName,
                source_group = source.sourceGroup,
                source_type = source.sourceType,
                enabled = source.enabled,
                lang = source.lang,
                custom_order = source.customOrder,
                comment = source.comment,
                last_update_time = source.lastUpdateTime,
                header_ = source.header,
                search_url = source.searchUrl,
                explore_url = source.exploreUrl,
                rule_search = json.encodeToString(source.ruleSearch),
                rule_book_info = json.encodeToString(source.ruleBookInfo),
                rule_toc = json.encodeToString(source.ruleToc),
                rule_content = json.encodeToString(source.ruleContent),
                rule_explore = json.encodeToString(source.ruleExplore)
            )
        }
    }
    
    override suspend fun upsertAll(sources: List<UserSource>) {
        handler.await(inTransaction = true) {
            sources.forEach { source ->
                userSourceQueries.insert(
                    source_url = source.sourceUrl,
                    source_name = source.sourceName,
                    source_group = source.sourceGroup,
                    source_type = source.sourceType,
                    enabled = source.enabled,
                    lang = source.lang,
                    custom_order = source.customOrder,
                    comment = source.comment,
                    last_update_time = source.lastUpdateTime,
                    header_ = source.header,
                    search_url = source.searchUrl,
                    explore_url = source.exploreUrl,
                    rule_search = json.encodeToString(source.ruleSearch),
                    rule_book_info = json.encodeToString(source.ruleBookInfo),
                    rule_toc = json.encodeToString(source.ruleToc),
                    rule_content = json.encodeToString(source.ruleContent),
                    rule_explore = json.encodeToString(source.ruleExplore)
                )
            }
        }
    }
    
    override suspend fun delete(sourceUrl: String) {
        handler.await { userSourceQueries.deleteByUrl(sourceUrl) }
    }
    
    override suspend fun deleteById(sourceId: Long) {
        handler.await { userSourceQueries.deleteById(sourceId) }
    }
    
    override suspend fun deleteAll() {
        handler.await { userSourceQueries.deleteAll() }
    }
    
    override suspend fun setEnabled(sourceUrl: String, enabled: Boolean) {
        handler.await { userSourceQueries.updateEnabled(enabled, sourceUrl) }
    }
    
    override suspend fun updateOrder(sourceUrl: String, newOrder: Int) {
        handler.await { userSourceQueries.updateOrder(newOrder, sourceUrl) }
    }
    
    override suspend fun getByGroup(group: String): List<UserSource> {
        return handler.awaitList { userSourceQueries.findByGroup(group) }
            .map { it.toUserSource() }
    }
    
    override suspend fun getGroups(): List<String> {
        return handler.awaitList { userSourceQueries.getGroups() }
    }
    
    override suspend fun exportToJson(): String {
        val sources = getAll()
        return json.encodeToString(sources)
    }
    
    override suspend fun importFromJson(jsonString: String): Result<Int> {
        return try {
            val trimmed = jsonString.trim()
            val imported: List<UserSource> = if (trimmed.startsWith("[")) {
                json.decodeFromString(trimmed)
            } else {
                listOf(json.decodeFromString(trimmed))
            }
            upsertAll(imported)
            Result.success(imported.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Extension to convert DB entity to domain model
    private fun User_source.toUserSource(): UserSource {
        return UserSource(
            sourceUrl = source_url,
            sourceName = source_name,
            sourceGroup = source_group,
            sourceType = source_type,
            enabled = enabled,
            lang = lang,
            customOrder = custom_order,
            comment = comment,
            lastUpdateTime = last_update_time,
            header = header_,
            searchUrl = search_url,
            exploreUrl = explore_url,
            ruleSearch = try { json.decodeFromString(rule_search) } catch (e: Exception) { SearchRule() },
            ruleBookInfo = try { json.decodeFromString(rule_book_info) } catch (e: Exception) { BookInfoRule() },
            ruleToc = try { json.decodeFromString(rule_toc) } catch (e: Exception) { TocRule() },
            ruleContent = try { json.decodeFromString(rule_content) } catch (e: Exception) { ContentRule() },
            ruleExplore = try { json.decodeFromString(rule_explore) } catch (e: Exception) { ExploreRule() }
        )
    }
}
