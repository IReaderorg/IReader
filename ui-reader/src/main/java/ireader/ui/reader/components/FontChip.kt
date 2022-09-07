package ireader.ui.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.ui.component.components.component.PreferenceRow
import ireader.ui.component.reusable_composable.CaptionTextComposable
import ireader.core.ui.ui.string
import ireader.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.ui.reader.R

@Composable
fun FontChip(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    onFontSelected: (Int) -> Unit,
) {
    PreferenceRow(
        title = string(id = R.string.font),
        action = {
            LazyRow {
                items(count = vm.fonts.size) { index ->
                    Spacer(modifier = modifier.width(10.dp))
                    Box(
                        modifier = modifier
                            .height(30.dp)
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                2.dp,
                                if (vm.fonts[index] == vm.font.value.name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                    .4f
                                ),
                                CircleShape
                            )
                            .clickable {
                                onFontSelected(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CaptionTextComposable(
                            text = vm.fonts.getOrNull(index) ?: "Unknown",
                            maxLine = 1,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    )
}
