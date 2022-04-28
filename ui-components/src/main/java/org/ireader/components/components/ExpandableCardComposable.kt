package org.ireader.components.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.core_ui.theme.Shapes


@ExperimentalMaterialApi
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
            ),
        shape = Shapes.medium,
        onClick = { isExpanded = !isExpanded },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h4.copy(
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
                color = MaterialTheme.colors.primary,
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


@ExperimentalMaterialApi
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExpandableCardComposablePrev() {
    ExpandableCardComposable(title = "Summary", description = """This is a brand new story.
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
After an incident, he gained supernatural powers…""")
}