package ireader.domain.models.notification

data class MigrationNotification(
    val bookTitle: String,
    val message: String,
    val isSuccess: Boolean = true
)
