package org.ireader.core_ui.ui

import androidx.compose.ui.res.stringResource

@androidx.compose.runtime.Composable
fun string(id:Int):String {
    return stringResource(id = id)
}