package ireader.presentation.ui.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
@Suppress("NO_ACTUAL_FOR_EXPECT")
@Composable
expect inline fun <reified VM : BaseViewModel> viewModel(): VM
@Suppress("NO_ACTUAL_FOR_EXPECT")
@Composable
expect inline fun <reified VM : BaseViewModel, S : Any> viewModel(
  noinline parameter: () -> S
): VM
@Suppress("NO_ACTUAL_FOR_EXPECT")
@Composable
expect inline fun <reified VM : BaseViewModel, S : Any> viewModel(
  noinline initialState: () -> S,
  saver: Saver<S, Any>
): VM
