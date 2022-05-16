package org.ireader.components.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.R
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.ui.Colour.Transparent

@Composable
fun LogoHeader() {
    Column {
        androidx.compose.material3.Surface(
            modifier = Modifier.background(MaterialTheme.colorScheme.Transparent).fillMaxWidth(),
            contentColor = AppColors.current.bars,
            color = AppColors.current.bars
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_eternity_full),
                contentDescription = null,
                tint =AppColors.current.onBars,
                modifier = Modifier
                    .padding(32.dp)
                    .size(100.dp),
            )
        }

        Divider()
    }
}
