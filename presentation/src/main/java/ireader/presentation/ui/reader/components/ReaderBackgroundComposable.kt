package ireader.presentation.ui.reader.components

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
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.domain.preferences.models.ReaderColors
import ireader.presentation.R
import ireader.presentation.ui.component.components.component.PreferenceRow
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel


@Composable
fun ReaderBackgroundComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
    onBackgroundChange: (themeId: Long) -> Unit,
    themes: List<ReaderColors>,
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
                                onBackgroundChange(themes[index].id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(60.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ),
                            imageVector = Icons.Default.Circle,
                            contentDescription = "color selected",
                            tint = themes[index].backgroundColor
                        )
                        if (viewModel.readerTheme.value.id == themes[index].id) {
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
