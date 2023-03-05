package ireader.presentation.ui.component.reusable_composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.components.IDropdownMenu
import ireader.presentation.ui.component.components.IDropdownMenuItem

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
            IDropdownMenu(
                modifier = Modifier
                    .fillMaxWidth(.5f),
                expanded = opened, // viewModel.state.isMenuExpanded,
                onDismissRequest = {
                    opened = false
                },
            ) {
                items.forEachIndexed { index, s ->
                    IDropdownMenuItem(onClick = {
                        opened = false
                        onSelected(index)
                    }, text = {
                            MidSizeTextComposable(text = s)
                        })
                }
            }
        }
    }
}