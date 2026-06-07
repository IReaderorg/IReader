package ireader.data.repository

import ireader.domain.data.repository.AnnouncementsRepository
import ireader.domain.data.repository.CommunityVotesRepository
import ireader.domain.data.repository.GamificationRepository
import ireader.domain.data.repository.SocialRepository
import ireader.domain.models.gamification.AchievementDef
import ireader.domain.models.gamification.AchievementView
import ireader.domain.models.gamification.CheckinResult
import ireader.domain.models.gamification.CommunityAnnouncement
import ireader.domain.models.gamification.FollowUser
import ireader.domain.models.gamification.GamificationProfile
import ireader.domain.models.gamification.OwnedTitle
import ireader.domain.models.gamification.ProfileComment
import ireader.domain.models.gamification.ReadingActivityItem
import ireader.domain.models.gamification.ReadingStatsSnapshot
import ireader.domain.models.gamification.SpiritStoneTxn
import ireader.domain.models.gamification.UnlockedAchievement

/** Fallbacks used when no Supabase backend is configured / signed out. Everything degrades to empty. */

object NoOpGamificationRepository : GamificationRepository {
    override suspend fun syncReadingStats(snapshot: ReadingStatsSnapshot) = Result.success(emptyList<UnlockedAchievement>())
    override suspend fun evaluate() = Result.success(emptyList<UnlockedAchievement>())
    override suspend fun getProfile(userId: String) = Result.success(GamificationProfile(userId = userId))
    override suspend fun updateProfile(displayName: String?, bio: String?, avatarUrl: String?, coverUrl: String?) = Result.success(Unit)
    override suspend fun getAchievementCatalog() = Result.success(emptyList<AchievementDef>())
    override suspend fun getAchievements(userId: String) = Result.success(emptyList<AchievementView>())
    override suspend fun getOwnedTitles(userId: String) = Result.success(emptyList<OwnedTitle>())
    override suspend fun setActiveTitle(titleId: String?) = Result.success(Unit)
    override suspend fun checkinDaily() = Result.success(CheckinResult(already = true, streakDay = 0, reward = 0))
    override suspend fun getStoneHistory(userId: String, limit: Int) = Result.success(emptyList<SpiritStoneTxn>())
    override suspend fun spendStones(itemType: String, itemId: String, cost: Int) = Result.success(0L)
}

object NoOpSocialRepository : SocialRepository {
    override suspend fun follow(targetUserId: String) = Result.success(Unit)
    override suspend fun unfollow(targetUserId: String) = Result.success(Unit)
    override suspend fun isFollowing(targetUserId: String) = Result.success(false)
    override suspend fun getFollowers(userId: String) = Result.success(emptyList<FollowUser>())
    override suspend fun getFollowing(userId: String) = Result.success(emptyList<FollowUser>())
    override suspend fun getFollowCounts(userId: String) = Result.success(0 to 0)
    override suspend fun getComments(profileUserId: String) = Result.success(emptyList<ProfileComment>())
    override suspend fun postComment(profileUserId: String, text: String) = Result.success(Unit)
    override suspend fun deleteComment(commentId: String) = Result.success(Unit)
    override suspend fun getActivity(userId: String, limit: Int) = Result.success(emptyList<ReadingActivityItem>())
    override suspend fun getFollowingActivity(limit: Int) = Result.success(emptyList<ReadingActivityItem>())
}

object NoOpCommunityVotesRepository : CommunityVotesRepository {
    override suspend fun vote(bookId: String) = Result.success(false)
    override suspend fun hasVotedToday(bookId: String) = Result.success(false)
    override suspend fun getVoteCount(bookId: String) = Result.success(0)
}

object NoOpAnnouncementsRepository : AnnouncementsRepository {
    override suspend fun getAnnouncements(limit: Int) = Result.success(emptyList<CommunityAnnouncement>())
    override suspend fun postAnnouncement(title: String, body: String, discordUrl: String?) = Result.success(Unit)
}
