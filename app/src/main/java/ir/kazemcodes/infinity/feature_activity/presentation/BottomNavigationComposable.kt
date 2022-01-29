package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationComposable(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel,
    items : List<BottomNavigationScreen>
) {
    val currentScreen: BottomNavigationScreen = viewModel.state.value.currentScreen
    BottomNavigation(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.background,
        elevation = 8.dp,
    ) {
        BottomNavigationBar(
            navController = navController,
            items = items,
            onItemClick = { viewModel.onEvent(MainScreenEvent.NavigateTo(currentScreen)) },
            viewModel = viewModel,
        )
        items.forEach { screen ->
            BottomNavigationItem(
                selected = screen.name == items[currentScreen.index].name,
                onClick = { viewModel.onEvent(MainScreenEvent.NavigateTo(screen))
                },
                label = { Text(text = screen.name) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = "${screen.name} icon"
                    )
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
            )

        }
    }
}