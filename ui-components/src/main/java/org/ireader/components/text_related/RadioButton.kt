package org.ireader.components.text_related

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.MidSizeTextComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioButton(
    modifier: Modifier = Modifier,
    text: UiText,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        androidx.compose.material3.RadioButton(selected = selected, onClick = { onClick() })
        Spacer(modifier = modifier.width(2.dp))
        MidSizeTextComposable(text = text)
    }
}
