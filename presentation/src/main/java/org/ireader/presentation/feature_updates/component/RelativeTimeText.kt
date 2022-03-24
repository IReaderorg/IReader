package org.ireader.presentation.feature_updates.component

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.core.utils.convertLongToTime

@Composable
fun RelativeTimeText(modifier: Modifier = Modifier, date: Long) {
    Text(
        text = convertLongToTime(date, format = "yyyy.MM.dd"),
        modifier = modifier,
        color = MaterialTheme.colors.onBackground
    )
}