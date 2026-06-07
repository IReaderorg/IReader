package ireader.domain.data.repository

import ireader.domain.models.gamification.CommunityAnnouncement
import ireader.domain.models.gamification.FollowUser
import ireader.domain.models.gamification.ProfileComment
import ireader.domain.models.gamification.ReadingActivityItem

/** Reader-to-reader social graph + profile walls + activity feed. */
interface SocialRepository {
    suspend fun follow(targetUserId: String): Result<Unit>
    suspend fun unfollow(targetUserId: String): Result<Unit>
    suspend fun isFollowing(targetUserId: String): Result<Boolean>
    suspend fun getFollowers(userId: String): Result<List<FollowUser>>
    suspend fun getFollowing(userId: String): Result<List<FollowUser>>
    suspend fun getFollowCounts(userId: String): Result<Pair<Int, Int>> // followers to following

    suspend fun getComments(profileUserId: String): Result<List<ProfileComment>>
    suspend fun postComment(profileUserId: String, text: String): Result<Unit>
    suspend fun deleteComment(commentId: String): Result<Unit>

    /** Activity feed for a single profile. */
    suspend fun getActivity(userId: String, limit: Int = 30): Result<List<ReadingActivityItem>>

    /** Aggregated activity from everyone the current user follows (discovery via trust). */
    suspend fun getFollowingActivity(limit: Int = 50): Result<List<ReadingActivityItem>>
}

/** Free daily power-stone voting (drives Trending; no currency cost, no author payout). */
interface CommunityVotesRepository {
    suspend fun vote(bookId: String): Result<Boolean>          // true if a new vote was recorded
    suspend fun hasVotedToday(bookId: String): Result<Boolean>
    suspend fun getVoteCount(bookId: String): Result<Int>
}

/** Admin-authored community news (no bot; admins post from the in-app admin panel). */
interface AnnouncementsRepository {
    suspend fun getAnnouncements(limit: Int = 20): Result<List<CommunityAnnouncement>>
    suspend fun postAnnouncement(title: String, body: String, discordUrl: String?): Result<Unit>
}
