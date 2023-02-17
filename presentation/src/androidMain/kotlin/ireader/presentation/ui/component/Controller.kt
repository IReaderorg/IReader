package ireader.presentation.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import ireader.presentation.ui.core.theme.CustomSystemColor

data class Controller @OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
) constructor(
    val navController: NavController,
    val navBackStackEntry: NavBackStackEntry,
    val requestHideNavigator: (Boolean) -> Unit = {},
    val requestHideSystemNavbar: (Boolean) -> Unit = {},
    val requestedHideSystemStatusBar: (Boolean) -> Unit = {},
    val requestedCustomSystemColor: (CustomSystemColor?) -> Unit = {},
    val setScrollBehavior: (TopAppBarScrollBehavior) -> Unit = {},
)
