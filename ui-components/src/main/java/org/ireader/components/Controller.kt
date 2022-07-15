package org.ireader.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.core_ui.theme.CustomSystemColor

data class Controller @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class) constructor(
    val navController: NavController,
    val navBackStackEntry: NavBackStackEntry,
    val snackBarHostState: SnackbarHostState,
    val sheetState: ModalBottomSheetState,
    val drawerState: DrawerState,
    val scaffoldPadding: PaddingValues = PaddingValues(0.dp),
    val requestHideNavigator: (Boolean) -> Unit = {},
    val requestHideTopAppbar: (Boolean) -> Unit = {},
    val requestHideSystemNavbar: (Boolean) -> Unit = {},
    val requestedHideSystemStatusBar: (Boolean) -> Unit = {},
    val requestedCustomSystemColor: (CustomSystemColor?) -> Unit = {},
    val scrollBehavior: TopAppBarScrollBehavior
)
