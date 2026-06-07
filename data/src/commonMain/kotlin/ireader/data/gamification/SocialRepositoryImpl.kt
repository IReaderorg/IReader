package ireader.data.gamification

import ireader.data.backend.BackendService
import ireader.domain.data.repository.AnnouncementsRepository
import ireader.domain.data.repository.CommunityVotesRepository
import ireader.domain.data.repository.SocialRepository
import ireader.domain.models.gamification.CommunityAnnouncement
import ireader.domain.models.gamification.FollowUser
import ireader.domain.models.gamification.ProfileComment
import ireader.domain.models.gamification.ReadingActivityItem
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class SocialRepositoryImpl(
    private val backend: BackendService,
    private val getCurrentUserId: suspend () -> String?,
) : SocialRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Serializable
    private data class FollowDto(
        @SerialName("follower_id") val followerId: String,
        @SerialName("following_id") val followingId: String,
    )

    @Serializable
    private data class UserBriefDto(
        @SerialName("id") val id: String,
        @SerialName("username") val username: String? = null,
        @SerialName("display_name") val displayName: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        @SerialName("level") val level: Int = 1,
    )

    @Serializable
    private data class CommentDto(
        @SerialName("id") val id: String,
        @SerialName("profile_user_id") val profileUserId: String,
        @SerialName("commenter_id") val commenterId: String,
        @SerialName("comment_text") val commentText: String,
        @SerialName("likes_count") val likesCount: Int = 0,
    )

    @Serializable
    private data class ActivityDto(
        @SerialName("id") val id: String,
        @SerialName("activity_type") val activityType: String,
        @SerialName("book_id") val bookId: String? = null,
        @SerialName("book_title") val bookTitle: String? = null,
        @SerialName("chapter_number") val chapterNumber: Int? = null,
        @SerialName("description") val description: String = "",
        @SerialName("user_id") val userId: String = "",
    )

    private suspend fun userBrief(id: String): FollowUser {
        val dto = backend.query("users", filters = mapOf("id" to id)).getOrNull()
            ?.firstOrNull()?.let { runCatching { json.decodeFromJsonElement(UserBriefDto.serializer(), it) }.getOrNull() }
        return FollowUser(
            userId = id,
            username = dto?.displayName ?: dto?.username ?: "Reader",
            avatarUrl = dto?.avatarUrl,
            level = dto?.level ?: 1,
        )
    }

    override suspend fun follow(targetUserId: String): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not signed in")
        backend.insert("user_follows", buildJsonObject {
            put("follower_id", uid); put("following_id", targetUserId)
        }, returning = false).getOrThrow()
        Unit
    }

    override suspend fun unfollow(targetUserId: String): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not signed in")
        backend.delete("user_follows", mapOf("follower_id" to uid, "following_id" to targetUserId)).getOrThrow()
    }

    override suspend fun isFollowing(targetUserId: String): Result<Boolean> = runCatching {
        val uid = getCurrentUserId() ?: return@runCatching false
        backend.query("user_follows", filters = mapOf("follower_id" to uid, "following_id" to targetUserId))
            .getOrThrow().isNotEmpty()
    }

    override suspend fun getFollowers(userId: String): Result<List<FollowUser>> = runCatching {
        backend.query("user_follows", filters = mapOf("following_id" to userId)).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(FollowDto.serializer(), it) }.getOrNull() }
            .map { userBrief(it.followerId) }
    }

    override suspend fun getFollowing(userId: String): Result<List<FollowUser>> = runCatching {
        backend.query("user_follows", filters = mapOf("follower_id" to userId)).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(FollowDto.serializer(), it) }.getOrNull() }
            .map { userBrief(it.followingId) }
    }

    override suspend fun getFollowCounts(userId: String): Result<Pair<Int, Int>> = runCatching {
        val followers = backend.query("user_follows", filters = mapOf("following_id" to userId), columns = "follower_id")
            .getOrThrow().size
        val following = backend.query("user_follows", filters = mapOf("follower_id" to userId), columns = "following_id")
            .getOrThrow().size
        followers to following
    }

    override suspend fun getComments(profileUserId: String): Result<List<ProfileComment>> = runCatching {
        backend.query("profile_comments", filters = mapOf("profile_user_id" to profileUserId),
            orderBy = "created_at", ascending = false).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(CommentDto.serializer(), it) }.getOrNull() }
            .map { dto ->
                val brief = userBrief(dto.commenterId)
                ProfileComment(
                    id = dto.id, profileUserId = dto.profileUserId, commenterId = dto.commenterId,
                    commenterName = brief.username, commenterAvatar = brief.avatarUrl,
                    text = dto.commentText, likes = dto.likesCount, createdAt = currentTimeToLong(),
                )
            }
    }

    override suspend fun postComment(profileUserId: String, text: String): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not signed in")
        backend.insert("profile_comments", buildJsonObject {
            put("profile_user_id", profileUserId); put("commenter_id", uid); put("comment_text", text)
        }, returning = false).getOrThrow()
        Unit
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> = runCatching {
        backend.delete("profile_comments", mapOf("id" to commentId)).getOrThrow()
    }

    override suspend fun getActivity(userId: String, limit: Int): Result<List<ReadingActivityItem>> = runCatching {
        backend.query("reading_activity", filters = mapOf("user_id" to userId),
            orderBy = "created_at", ascending = false, limit = limit).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(ActivityDto.serializer(), it) }.getOrNull() }
            .map { it.toDomain() }
    }

    override suspend fun getFollowingActivity(limit: Int): Result<List<ReadingActivityItem>> = runCatching {
        val uid = getCurrentUserId() ?: return@runCatching emptyList()
        val followingIds = backend.query("user_follows", filters = mapOf("follower_id" to uid), columns = "following_id")
            .getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(FollowDto.serializer(), it) }.getOrNull()?.followingId }
        // Two-step fan-out (no IN filter in BackendService); acceptable for typical follow counts.
        followingIds.flatMap { fid ->
            getActivity(fid, limit = 10).getOrNull().orEmpty()
        }.sortedByDescending { it.createdAt }.take(limit)
    }

    private fun ActivityDto.toDomain() = ReadingActivityItem(
        id = id, type = activityType, bookId = bookId, bookTitle = bookTitle,
        chapterNumber = chapterNumber, description = description, createdAt = currentTimeToLong(),
    )
}

class CommunityVotesRepositoryImpl(
    private val backend: BackendService,
    private val getCurrentUserId: suspend () -> String?,
) : CommunityVotesRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun vote(bookId: String): Result<Boolean> = runCatching {
        val obj = backend.rpc("vote_book", mapOf("p_book_id" to bookId)).getOrThrow().jsonObject
        obj["voted"]?.jsonPrimitive?.booleanOrNull ?: false
    }

    override suspend fun hasVotedToday(bookId: String): Result<Boolean> = runCatching {
        val uid = getCurrentUserId() ?: return@runCatching false
        backend.query("power_stone_votes", filters = mapOf("user_id" to uid, "book_id" to bookId))
            .getOrThrow().isNotEmpty()
    }

    override suspend fun getVoteCount(bookId: String): Result<Int> = runCatching {
        backend.query("power_stone_votes", filters = mapOf("book_id" to bookId), columns = "id").getOrThrow().size
    }
}

class AnnouncementsRepositoryImpl(
    private val backend: BackendService,
    private val getCurrentUserId: suspend () -> String?,
) : AnnouncementsRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Serializable
    private data class AnnDto(
        @SerialName("id") val id: String,
        @SerialName("title") val title: String? = null,
        @SerialName("body") val body: String? = null,
        @SerialName("discord_message_url") val discordUrl: String? = null,
    )

    override suspend fun getAnnouncements(limit: Int): Result<List<CommunityAnnouncement>> = runCatching {
        backend.query("community_announcements", orderBy = "posted_at", ascending = false, limit = limit).getOrThrow()
            .mapNotNull { runCatching { json.decodeFromJsonElement(AnnDto.serializer(), it) }.getOrNull() }
            .map { CommunityAnnouncement(it.id, it.title, it.body, it.discordUrl, currentTimeToLong()) }
    }

    override suspend fun postAnnouncement(title: String, body: String, discordUrl: String?): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not signed in")
        backend.insert("community_announcements", buildJsonObject {
            put("title", title); put("body", body); put("author_id", uid)
            if (discordUrl != null) put("discord_message_url", discordUrl)
        }, returning = false).getOrThrow()
        Unit
    }
}
