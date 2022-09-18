package ireader.ui.component.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.ui.core.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCardComposable(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.W400,
    maxLine: Int = 5,
    durationMillis: Int = 500,

) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LinearOutSlowInEasing
                )
            )
            .clickable {
                isExpanded = !isExpanded
            }.background(color = MaterialTheme.colorScheme.background),
        shape = Shapes.medium,
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
            )
            Text(
                text = description,
                maxLines = if (!isExpanded) maxLine else 100,
                overflow = TextOverflow.Ellipsis,
                softWrap = true, fontSize = fontSize,
                fontWeight = fontWeight,

            )

            Text(
                text = "Show More",
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier
                    .align(Alignment.End)
                    .clickable { isExpanded = !isExpanded }
            )

//            if (isExpanded) {
//                Text(
//                    text = description,
//                    fontSize = fontSize,
//                    fontWeight = fontWeight,
//                    maxLines = maxLine
//                )
//            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExpandableCardComposablePrev() {
    ExpandableCardComposable(
        title = "Summary",
        description = """This is a brand new story.
Survive the darkness, see the light
There is no right or wrong, it just depends on which side you are standing on.
To be a god, or to be a man.
To be good, or to be evil.
Just what is…the highest order of weapon that humanity has?
————————————————————————————————————————
After a great catastrophe struck, the world was set back many years and humanity started living in anarchy.
With time, society started building up again and people were now living in walled strongholds and fringe towns across the land.
Humans have also become distrustful and ruthless in an unforgiving society where the strong survive and the weak are eliminated.
Growing up in such an era, Ren Xiaosu had to fend for himself.
After an incident, he gained supernatural powers…"""
    )
}
