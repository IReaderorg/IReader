package ireader.domain.models.quote

import kotlinx.serialization.Serializable

/**
 * Reading Buddy state - a cute rabbit companion that reacts to reading progress
 */
@Serializable
data class ReadingBuddyState(
    val mood: BuddyMood = BuddyMood.HAPPY,
    val message: String = "",
    val animation: BuddyAnimation = BuddyAnimation.IDLE,
    val level: Int = 1,
    val experience: Int = 0,
    val totalBooksRead: Int = 0,
    val totalChaptersRead: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastInteractionTime: Long = 0
)

/**
 * Buddy mood states that affect appearance and messages
 */
enum class BuddyMood(val emoji: String, val description: String) {
    SLEEPING("😴", "Zzz... Wake me up when you read!"),
    SLEEPY("🥱", "Haven't seen you in a while..."),
    SAD("😢", "I miss reading with you..."),
    NEUTRAL("🐰", "Ready to read together?"),
    HAPPY("😊", "Great to see you!"),
    EXCITED("🤩", "You're on fire!"),
    CELEBRATING("🎉", "Amazing achievement!"),
    PROUD("🏆", "Look at you go!"),
    READING("📖", "Reading together..."),
    CHEERING("📣", "Keep going!")
}

/**
 * Buddy animations for different events
 */
enum class BuddyAnimation {
    IDLE,
    WAVE,
    JUMP,
    DANCE,
    READ,
    SLEEP,
    CELEBRATE,
    CHEER,
    SPARKLE,
    BOUNCE
}

/**
 * Achievement types the buddy can celebrate
 */
enum class BuddyAchievement(val title: String, val description: String, val xpReward: Int) {
    FIRST_CHAPTER("First Steps", "Read your first chapter", 10),
    FIRST_BOOK("Bookworm Begins", "Finish your first book", 50),
    STREAK_3("Getting Started", "3 day reading streak", 30),
    STREAK_7("Week Warrior", "7 day reading streak", 70),
    STREAK_30("Monthly Master", "30 day reading streak", 300),
    BOOKS_5("Avid Reader", "Read 5 books", 100),
    BOOKS_10("Book Lover", "Read 10 books", 200),
    BOOKS_25("Library Regular", "Read 25 books", 500),
    BOOKS_50("Bibliophile", "Read 50 books", 1000),
    CHAPTERS_100("Century Club", "Read 100 chapters", 150),
    NIGHT_OWL("Night Owl", "Read after midnight", 25),
    EARLY_BIRD("Early Bird", "Read before 6 AM", 25),
    MARATHON("Marathon Reader", "Read for 2+ hours", 75),
    QUOTE_COLLECTOR("Quote Collector", "Save 10 quotes", 50)
}

/**
 * Messages the buddy can say based on context
 */
object BuddyMessages {
    val greetings = listOf(
        "Hey there, bookworm! 📚",
        "Ready for an adventure? 🌟",
        "Let's dive into a story! 🐰",
        "Your reading buddy is here! 💕",
        "Time for some reading magic! ✨"
    )
    
    val encouragements = listOf(
        "You're doing great! Keep reading! 📖",
        "One more chapter? I believe in you! 💪",
        "Your reading streak is impressive! 🔥",
        "The story awaits! Let's go! 🚀",
        "I love reading with you! 💕"
    )
    
    val milestones = listOf(
        "WOW! You finished a chapter! 🎉",
        "Another book conquered! You're amazing! 🏆",
        "New streak record! So proud of you! 🌟",
        "Level up! You're becoming a master reader! ⬆️",
        "Achievement unlocked! 🎊"
    )
    
    val comebacks = listOf(
        "Welcome back! I missed you! 🥺",
        "You're here! Let's read together! 💕",
        "I was waiting for you! 🐰",
        "Ready to continue our adventure? 📚",
        "So happy to see you again! 🌟"
    )
    
    val sleepy = listOf(
        "Zzz... *yawn* ...is it reading time? 😴",
        "I've been napping... waiting for you... 🥱",
        "Wake me up when you want to read! 💤",
        "*stretches* Ready when you are! 🐰"
    )
}
