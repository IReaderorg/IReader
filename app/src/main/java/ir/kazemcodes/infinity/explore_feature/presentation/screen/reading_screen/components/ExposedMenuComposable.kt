package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kazemcodes.infinity.base_feature.theme.poppins
import ir.kazemcodes.infinity.base_feature.theme.sourceSansPro


@ExperimentalMaterialApi
@Composable
fun FontMenuComposable(
    modifier: Modifier = Modifier,
    onClick : (selectedFont : FontFamily , fontName : String) -> Unit
) {
    var selectedFontName by remember { mutableStateOf("Poppins") }
    var expanded by remember { mutableStateOf(false) }
    var selectedFont by remember { mutableStateOf(poppins) }



    Row(
        modifier = modifier.fillMaxWidth().padding(end = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,

    ) {
        Text(
            modifier = modifier.fillMaxWidth(.2f),
            text = "Font",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )
        Box(
            modifier = modifier
                .fillMaxWidth(.8f)
                .clickable {
                    onClick(selectedFont, selectedFontName)
                    expanded = !expanded
                }
                .border(.8.dp, color = MaterialTheme.colors.onBackground.copy(.5f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = selectedFontName)
            DropdownMenu(expanded = expanded, onDismissRequest = {expanded = false},) {
                DropdownMenuItem(onClick = {
                    selectedFont = poppins
                    selectedFontName = "Poppins"
                    onClick(selectedFont , selectedFontName)
                }) {
                    Text(text = "Poppins")
                }
                DropdownMenuItem(onClick = {
                    selectedFont  = sourceSansPro
                    selectedFontName = "Source Sans Pro"
                    onClick(selectedFont , selectedFontName)
                }) {
                    Text(text = "Source Sans Pro")
                }
            }
        }
    }


}


//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = {
//            expanded = !expanded
//        },
//        modifier.fillMaxWidth()
//    ) {
//        TextField(
//            readOnly = true,
//            value = selectedOptionText,
//            onValueChange = { },
//            label = { Text("Font") },
//            trailingIcon = {
//                ExposedDropdownMenuDefaults.TrailingIcon(
//                    expanded = expanded
//                )
//            },
//            colors = ExposedDropdownMenuDefaults.textFieldColors()
//        )
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = {
//                expanded = false
//            }
//        ) {
//            options.forEach { selectionOption ->
//                DropdownMenuItem(
//                    onClick = {
//                        onClick(selectedFont[options.indexOf(selectedOptionText)])
//                        selectedOptionText = selectionOption
//                        expanded = false
//                    }
//                ) {
//                    Text(text = selectionOption)
//                }
//            }
//        }
//    }
//}
//@ExperimentalComposeUiApi
//@Composable
//fun ComposeMenu(
//    menuItems: List<String>,
//    menuExpandedState: Boolean,
//    seletedIndex : Int,
//    updateMenuExpandStatus : () -> Unit,
//    onDismissMenuView : () -> Unit,
//    onMenuItemclick : (Int) -> Unit,
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .wrapContentSize(Alignment.TopStart)
//            .padding(top = 10.dp)
//            .border(0.5.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
//            .clickable(
//                onClick = {
//                    updateMenuExpandStatus()
//                },
//            ),
//
//        ) {
//
//        BoxWithConstraints(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//
//            val (lable, iconView) = createRefs()
//
//            Text(
//                text = menuItems[seletedIndex],
//                color = seletedIndex.dropdownDisableColor(),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .constrainAs(lable) {
//                        top.linkTo(parent.top)
//                        bottom.linkTo(parent.bottom)
//                        start.linkTo(parent.start)
//                        end.linkTo(iconView.start)
//                        width = Dimension.fillToConstraints
//                    }
//            )
//
//            val displayIcon: Painter = painterResource(
//                id = R.drawable.ic_drop_down
//            )
//
//            Icon(
//                painter = displayIcon,
//                contentDescription = null,
//                modifier = Modifier
//                    .size(20.dp, 20.dp)
//                    .constrainAs(iconView) {
//                        end.linkTo(parent.end)
//                        top.linkTo(parent.top)
//                        bottom.linkTo(parent.bottom)
//                    },
//                tint = MaterialTheme.colors.onSurface
//            )
//
//            DropdownMenu(
//                expanded = menuExpandedState,
//                onDismissRequest = { onDismissMenuView() },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colors.surface)
//            ) {
//                menuItems.forEachIndexed { index, title ->
//                    DropdownMenuItem(
//                        onClick = {
//                            if (index != 0) {
//                                onMenuItemclick(index)
//                            }
//                        }) {
//                        Text(text = title)
//                    }
//                }
//            }
//        }
//    }
//}
//
