package ireader.presentation.ui.home.sources.extension.composables

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.widget.EmojiTextView

@Composable
fun EmojiText(
    text: String,
    modifier: Modifier,
) {
    AndroidView(
        factory = { EmojiTextView(it, null).apply { setTextColor(Color.BLACK) } },
        modifier = modifier,
        update = { it.text = text }
    )
}
