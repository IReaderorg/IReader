package ireader.data.gamification

import ireader.data.backend.BackendService
import ireader.domain.data.repository.GamificationRepository
import ireader.domain.models.gamification.AchievementDef
import ireader.domain.models.gamification.AchievementView
import ireader.domain.models.gamification.CheckinResult
import ireader.domain.models.gamification.GamificationProfile
import ireader.domain.models.gamification.OwnedTitle
import ireader.domain.models.gamification.ReadingStatsSnapshot
import ireader.domain.models.gamification.SpiritStoneTxn
import ireader.domain.models.gamification.UnlockedAchievement
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class GamificationRepositoryImpl(
    private val backend: BackendService,
    private val getCurrentUserId: suspend () -> String?,
) : GamificationRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Serializable
    private data class UnlockDto(
        @SerialName("achievement_id") val achievementId: String,
        @SerialName("name") val name: String = "",
        @SerialName("icon") val icon: String = "🏅",
        @SerialName("image_url") val imageUrl: String? = null,
        @SerialName("tier") val tier: String = "BRONZE",
        @SerialName("reward_xp") val rewardXp: Int = 0,
        @SerialName("reward_stones") val rewardStones: Int = 0,
    )

    @Serializable
    private data class UserDto(
        @SerialName("id") val id: String,
        @SerialName("level") val level: Int = 1,
        @SerialName("xp") val xp: Long = 0,
        @SerialName("level_title") val levelTitle: String = "Novice Reader",
        @SerialName("spirit_stones") val spiritStones: Long = 0,
        @SerialName("checkin_streak") val checkinStreak: Int = 0,
        @SerialName("active_title_id") val activeTitleId: String? = null,
        @SerialName("discord_id") val discordId: String? = null,
        @SerialName("discord_username") val discordUsername: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        @SerialName("cover_image_url") val coverUrl: String? = null,
        @SerialName("bio") val bio: String? = null,
        @SerialName("display_name") val displayName: String? = null,
        @SerialName("created_at") val createdAt: String? = null,
    )

    @Serializable
    private data class DefDto(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("description") val description: String = "",
        @SerialName("icon") val icon: String = "🏅",
        @SerialName("image_url") val imageUrl: String? = null,
        @SerialName("category") val category: String = "",
        @SerialName("tier") val tier: String = "BRONZE",
        @SerialName("metric") val metric: String = "",
        @SerialName("threshold") val threshold: Long = 1,
        @SerialName("reward_xp") val rewardXp: Int = 0,
        @SerialName("reward_stones") val rewardStones: Int = 0,
        @SerialName("is_secret") val isSecret: Boolean = false,
        @SerialName("sort_order") val sortOrder: Int = 0,
    )

    @Serializable
    private data class UserAchvDto(
        @SerialName("achievement_id") val achievementId: String,
        @SerialName("progress") val progress: Long = 0,
        @SerialName("is_completed") val isCompleted: Boolean = false,
    )

    @Serializable
    private data class TitleDto(
        @SerialName("title_id") val titleId: String,
        @SerialName("title_name") val titleName: String,
        @SerialName("rarity") val rarity: String = "COMMON",
        @SerialName("is_active") val isActive: Boolean = false,
    )

    @Serializable
    private data class TxnDto(
        @SerialName("amount") val amount: Long,
        @SerialName("type") val type: String,
        @SerialName("description") val description: String = "",
    )

    private fun parseUnlocks(el: JsonElement): List<UnlockedAchievement> {
        val arr = el as? JsonArray ?: return emptyList()
        return arr.mapNotNull { row ->
            runCatching { json.decodeFromJsonElement(UnlockDto.serializer(), row) }.getOrNull()
        }.map {
            UnlockedAchievement(it.achievementId, it.name, it.icon, it.imageUrl, it.tier, it.rewardXp, it.rewardStones)
        }
    }

    override suspend fun syncReadingStats(snapshot: ReadingStatsSnapshot): Result<List<UnlockedAchievement>> =
        runCatching {
            val params = mapOf(
                "p_minutes" to snapshot.minutes,
                "p_chapters" to snapshot.chapters,
                "p_books" to snapshot.books,
                "p_streak" to snapshot.streak,
                "p_longest" to snapshot.longestStreak,
                "p_avg_wpm" to snapshot.avgWpm,
                "p_genres" to snapshot.genresExplored,
            )
            parseUnlocks(backend.rpc("sync_reading_stats", params).getOrThrow())
        }

    override suspend fun evaluate(): Result<List<UnlockedAchievement>> = runCatching {
        val uid = getCurrentUserId() ?: return@runCatching emptyList()
        parseUnlocks(backend.rpc("evaluate_achievements", mapOf("p_user" to uid)).getOrThrow())
    }

    override suspend fun getProfile(userId: String): Result<GamificationProfile> = runCatching {
        val rows = backend.query("users", filters = mapOf("id" to userId)).getOrThrow()
        val dto = rows.firstOrNull()?.let { json.decodeFromJsonElement(UserDto.serializer(), it) }
            ?: return@runCatching GamificationProfile(userId = userId)
        GamificationProfile(
            userId = dto.id,
            level = dto.level,
            xp = dto.xp,
            levelTitle = dto.levelTitle,
            spiritStones = dto.spiritStones,
            checkinStreak = dto.checkinStreak,
            activeTitleId = dto.activeTitleId,
            discordLinked = dto.discordId != null,
            discordUsername = dto.discordUsername,
            avatarUrl = dto.avatarUrl,
            coverUrl = dto.coverUrl,
            bio = dto.bio ?: "",
            displayName = dto.displayName,
            joinedAt = dto.createdAt,
        )
    }

    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        avatarUrl: String?,
        coverUrl: String?,
    ): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not signed in")
        val data = buildJsonObject {
            if (displayName != null) put("display_name", displayName)
            if (bio != null) put("bio", bio)
            if (avatarUrl != null) put("avatar_url", avatarUrl)
            if (coverUrl != null) put("cover_image_url", coverUrl)
        }
        backend.update("users", filters = mapOf("id" to uid), data = data, returning = false).getOrThrow()
        Unit
    }

    override suspend fun getAchievementCatalog(): Result<List<AchievementDef>> = runCatching {
        backend.query("achievement_definitions", filters = mapOf("is_active" to true), orderBy = "sort_order")
            .getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(DefDto.serializer(), it) }.getOrNull() }
            .map { it.toDomain() }
    }

    override suspend fun getAchievements(userId: String): Result<List<AchievementView>> = runCatching {
        val defs = getAchievementCatalog().getOrThrow()
        val progressByid = backend.query("user_achievements", filters = mapOf("user_id" to userId)).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(UserAchvDto.serializer(), it) }.getOrNull() }
            .associateBy { it.achievementId }
        defs.map { def ->
            val p = progressByid[def.id]
            AchievementView(
                def = def,
                progress = p?.progress ?: 0,
                isCompleted = p?.isCompleted ?: false,
                earnedAt = null,
            )
        }
    }

    override suspend fun getOwnedTitles(userId: String): Result<List<OwnedTitle>> = runCatching {
        backend.query("user_titles", filters = mapOf("user_id" to userId)).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(TitleDto.serializer(), it) }.getOrNull() }
            .map { OwnedTitle(it.titleId, it.titleName, it.rarity, it.isActive, currentTimeToLong()) }
    }

    override suspend fun setActiveTitle(titleId: String?): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not signed in")
        // Deactivate all, then activate the chosen one (and reflect on users.active_title_id)
        backend.update(
            table = "user_titles",
            filters = mapOf("user_id" to uid),
            data = buildJsonObject { put("is_active", false) },
            returning = false,
        ).getOrThrow()
        if (titleId != null) {
            backend.update(
                table = "user_titles",
                filters = mapOf("user_id" to uid, "title_id" to titleId),
                data = buildJsonObject { put("is_active", true) },
                returning = false,
            ).getOrThrow()
        }
        Unit
    }

    override suspend fun checkinDaily(): Result<CheckinResult> = runCatching {
        val obj = backend.rpc("checkin_daily").getOrThrow().jsonObject
        CheckinResult(
            already = obj["already"]?.jsonPrimitive?.booleanOrNull ?: false,
            streakDay = obj["streak_day"]?.jsonPrimitive?.intOrNull ?: 0,
            reward = obj["reward"]?.jsonPrimitive?.intOrNull ?: 0,
        )
    }

    override suspend fun getStoneHistory(userId: String, limit: Int): Result<List<SpiritStoneTxn>> = runCatching {
        backend.query(
            "spirit_stone_transactions",
            filters = mapOf("user_id" to userId),
            orderBy = "created_at",
            ascending = false,
            limit = limit,
        ).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(TxnDto.serializer(), it) }.getOrNull() }
            .map { SpiritStoneTxn(it.amount, it.type, it.description, currentTimeToLong()) }
    }

    override suspend fun spendStones(itemType: String, itemId: String, cost: Int): Result<Long> = runCatching {
        val obj = backend.rpc(
            "spend_stones",
            mapOf("p_item_type" to itemType, "p_item_id" to itemId, "p_cost" to cost),
        ).getOrThrow().jsonObject
        val ok = obj["ok"]?.jsonPrimitive?.booleanOrNull ?: false
        if (!ok) error(obj["reason"]?.jsonPrimitive?.content ?: "SPEND_FAILED")
        obj["balance"]?.jsonPrimitive?.longOrNull ?: 0L
    }

    private fun DefDto.toDomain() = AchievementDef(
        id, name, description, icon, imageUrl, category, tier, metric, threshold, rewardXp, rewardStones, isSecret,
    )
}
