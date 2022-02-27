package org.ireader.core_ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import tachiyomi.core.di.AppScope
import tachiyomi.core.di.close
import toothpick.ktp.binding.module

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
@Composable
@PublishedApi
internal inline fun <reified VM : BaseViewModel> viewModel(): VM {
    val factory = ViewModelFactory()
    return viewModel(VM::class.java, factory)
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
@Composable
inline fun <reified VM : BaseViewModel, S : Any> viewModel(
    noinline initialState: () -> S,
): VM {
    val state = remember(calculation = initialState)
    val factory = ViewModelWithStateFactory(state = state)
    return viewModel(VM::class.java, factory)
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
@Composable
inline fun <reified VM : BaseViewModel, S : Any> viewModel(
    noinline initialState: () -> S,
    saver: Saver<S, Any>,
): VM {
    val state = rememberSaveable(init = initialState, saver = saver)
    val factory = ViewModelWithStateFactory(state = state)
    return viewModel(VM::class.java, factory)
}

@Composable
private fun <VM : BaseViewModel> viewModel(
    vmClass: Class<VM>,
    factory: ViewModelProvider.Factory,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
): VM {
    val vm = ViewModelProvider(viewModelStoreOwner, factory).get(vmClass)
    DisposableEffect(vm) {
        vm.setActive()
        onDispose {
            vm.setInactive()
        }
    }
    return vm
}

internal class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppScope.getInstance(modelClass)
    }
}

internal class ViewModelWithStateFactory(
    private val state: Any,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val submodule = module {
            bind(state.javaClass).toInstance(state)
        }
        val subscope = AppScope.subscope(submodule).apply {
            installModules(submodule)
        }

        val viewModel = subscope.getInstance(modelClass)

        callbackFlow<Nothing> {
            awaitClose { subscope.close() }
        }.launchIn(viewModel.viewModelScope)

        return viewModel
    }

}
