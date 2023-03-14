package ireader.presentation.ui.component.list.scrollbars


import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp


/*Copyright 2015 Javier Tomás

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

/**
 * Code Taken from tachiyomi
 * https://github.com/tachiyomiorg/tachiyomi
 */

@Composable
expect fun VerticalFastScroller(
    listState: LazyListState,
    modifier: Modifier,
    thumbAllowed: () -> Boolean ,
    thumbColor: Color,
    topContentPadding: Dp,
    bottomContentPadding: Dp ,
    endContentPadding: Dp ,
    content: @Composable () -> Unit,
)
@Composable
fun IVerticalFastScroller(
        listState: LazyListState,
        modifier: Modifier = Modifier,
        thumbAllowed: () -> Boolean = { true },
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        topContentPadding: Dp = Dp.Hairline,
        bottomContentPadding: Dp = Dp.Hairline,
        endContentPadding: Dp = Dp.Hairline,
        content: @Composable () -> Unit,
) = VerticalFastScroller(listState, modifier, thumbAllowed, thumbColor, topContentPadding, bottomContentPadding, endContentPadding, content)


expect fun scrollbarFadeDuration() : Int