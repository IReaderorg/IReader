package org.ireader.components.reusable_composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DropDownMenu(
    text: String,
    onSelected: (key: Int) -> Unit,
    currentValue: String? = null,
    items: Array<String>,
) {
    var opened by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MidSizeTextComposable(modifier = Modifier.weight(1f), text = text)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        opened = true
                    }
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MidSizeTextComposable(text = currentValue ?: items.first())
                AppIconButton(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    onClick = {
                        opened = true
                    }
                )
            }
            DropdownMenu(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth(.5f),
                expanded = opened, // viewModel.state.isMenuExpanded,
                onDismissRequest = {
                    opened = false
                },
            ) {
                items.forEachIndexed { index, s ->
                    DropdownMenuItem(onClick = {
                        opened = false
                        onSelected(index)
                    },text = {
                        MidSizeTextComposable(text = s)
                    })
                }
            }
        }
    }
}

@Composable
fun SortedByScreen() {
}

@Preview(showBackground = true)
@Composable
fun FilterItemPrev() {
    DropDownMenu(
      "Sort By:",
        { s ->
        },
      "Latest",
        arrayOf(
        "Latest",
           "Popular"
        )
    )
}
