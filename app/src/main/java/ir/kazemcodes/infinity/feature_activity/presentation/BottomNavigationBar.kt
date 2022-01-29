package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen

@ExperimentalMaterialApi
@Composable
fun BottomNavigationBar(
    items: List<BottomNavigationScreen>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavigationScreen) -> Unit,
    viewModel : MainViewModel
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.background,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            val selected = item.name == viewModel.state.value.currentScreen.name
            BottomNavigationItem(
                selected = selected,
                onClick = { viewModel.onEvent(MainScreenEvent.NavigateTo(item)) },
                label = { Text(text = item.name) },
                icon = {
                    Column(horizontalAlignment = CenterHorizontally) {
                        if(item.badgeCount > 0) {
                            BadgedBox(
                                badge = {
                                    Text(text = item.badgeCount.toString())
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name
                            )
                        }
                    }
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
            )
        }
    }
}