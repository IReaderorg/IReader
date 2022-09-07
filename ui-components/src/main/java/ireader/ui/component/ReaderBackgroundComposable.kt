package ireader.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ireader.ui.component.components.component.PreferenceRow
import ireader.core.ui.modifier.clickableNoIndication
import ireader.core.ui.theme.ReaderColors


@Composable
fun ThemePreference(
    modifier: Modifier = Modifier,
    onBackgroundChange: (color:ReaderColors) -> Unit,
    themes: List<ReaderColors>,
    selected:(color:ReaderColors) -> Boolean
) {

    PreferenceRow(
        modifier = Modifier.height(80.dp),
        title = stringResource(R.string.background_color),
        action = {
            LazyRow(
                contentPadding = PaddingValues(4.dp)
            ) {
                items(themes.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickableNoIndication {
                                onBackgroundChange(themes[index])
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(50.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ),
                            imageVector = Icons.Default.Circle,
                            contentDescription = "color selected",
                            tint = themes[index].backgroundColor
                        )
                        if (selected(themes[index])) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "color selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        })
}