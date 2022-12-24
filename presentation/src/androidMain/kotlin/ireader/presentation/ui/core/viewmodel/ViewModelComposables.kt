package ireader.presentation.ui.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.androidx.compose.getViewModel


@Composable
actual inline fun <reified VM : BaseViewModel> viewModel(): VM {
  return getViewModel<VM>()
}


@Composable
private fun <VM : BaseViewModel> viewModel(
  vmClass: Class<VM>,
  factory: ViewModelProvider.Factory,
  viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
): VM {
  val vm = ViewModelProvider(viewModelStoreOwner, factory)[vmClass]
  DisposableEffect(vm) {
    vm.setActive()
    onDispose {
      vm.setInactive()
    }
  }
  return vm
}

