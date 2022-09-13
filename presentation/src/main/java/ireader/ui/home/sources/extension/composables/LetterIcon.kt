

package ireader.ui.home.sources.extension.composables

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun LetterIcon(
    text: String,
    modifier: Modifier = Modifier,
) {
    val letter = remember(text) {
        if (text.isEmpty()) " " else text.take(1)
    }
    val color = remember(letter) {
        letterIconColors.let { it[Random.nextInt(it.size)] }
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color
    ) {
        Text(
            text = letter,
            color = Color.White,
            modifier = Modifier.wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

private val letterIconColors
    get() = arrayOf(
        Color(0xffe57373),
        Color(0xfff06292),
        Color(0xffba68c8),
        Color(0xff9575cd),
        Color(0xff7986cb),
        Color(0xff64b5f6),
        Color(0xff4fc3f7),
        Color(0xff4dd0e1),
        Color(0xff4db6ac),
        Color(0xff81c784),
        Color(0xffaed581),
        Color(0xffff8a65),
        Color(0xffd4e157),
        Color(0xffffd54f),
        Color(0xffffb74d),
        Color(0xffa1887f),
        Color(0xff90a4ae)
    )
