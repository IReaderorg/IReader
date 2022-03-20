package org.ireader.presentation.feature_detail.presentation.book_detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.SuperSmallTextComposable

@Composable
fun CardTileComposable(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    trailing: @Composable RowScope.() -> Unit = {},
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.Center, modifier = modifier.clickable {
        onClick()
    }) {
        Text(
            modifier = modifier,
            text = title,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )
        Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = subtitle, color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle2
            )
            trailing()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdvanceSettingItem(
    title: String = "",
    subtitle: String = "",
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onClick() },
        text = {
            MidSizeTextComposable(text = title)
        },
        secondaryText = {
            SuperSmallTextComposable(text = subtitle)
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AdvanceSettingPrev() {
    AdvanceSettingItem(title = "Clear All Table", subtitle = "38.6 kb")
}