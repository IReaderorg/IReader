package ireader.presentation.ui.book.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Dialog shown when user tries to translate more than 10 chapters at once.
 * Warns about rate limiting and estimated time.
 */
@Composable
fun TranslationWarningDialog(
    chapterCount: Int,
    estimatedMinutes: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = localizeHelper.localize(Res.string.translation_warning),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = buildString {
                    append("You are about to translate $chapterCount chapters.\n\n")
                    append("To avoid rate limiting from translation services, ")
                    append("there will be a delay between each chapter.\n\n")
                    if (estimatedMinutes > 0) {
                        append("Estimated time: ~$estimatedMinutes minutes\n\n")
                    }
                    append("Do you want to continue?")
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localizeHelper.localize(Res.string.continues))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
