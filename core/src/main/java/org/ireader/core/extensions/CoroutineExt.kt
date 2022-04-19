package org.ireader.core.extensions


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread


/**
 * Created by hristijan on 5/27/19 to long live and prosper !
 */

suspend inline fun <T, R> T.onMain(crossinline block: (T) -> R): R = withContext(mainDispatcher) { this@onMain.let(block) }
suspend inline fun <T> onMain(crossinline block: CoroutineScope.() -> T): T = withContext(mainDispatcher) { block.invoke(this@withContext) }


suspend inline fun <T, R> T.nonCancellable(crossinline block: (T) -> R): R = withContext(NonCancellable) { this@nonCancellable.let(block) }
suspend inline fun <T> nonCancellable(crossinline block: CoroutineScope.() -> T): T = withContext(NonCancellable) { block.invoke(this@withContext) }


suspend inline fun <T> onDefault(crossinline block: CoroutineScope.() -> T): T = withContext(defaultDispatcher) { block.invoke(this@withContext) }
suspend inline fun <T, R> T.onDefault(crossinline block: (T) -> R): R = withContext(defaultDispatcher) { this@onDefault.let(block) }


suspend inline fun <T, R> T.onIO(crossinline block: (T) -> R): R = withContext(ioDispatcher) { this@onIO.let(block) }
suspend inline fun <T> onIO(crossinline block: CoroutineScope.() -> T): T = withContext(ioDispatcher) { block.invoke(this@withContext) }


val mainDispatcher = Dispatchers.Main
val defaultDispatcher = Dispatchers.Default
val unconfinedDispatcher = Dispatchers.Unconfined
val ioDispatcher = Dispatchers.IO


fun <T> ioCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job =
    GlobalScope.launch(ioDispatcher, coroutineStart) {
        block()
    }


fun <T> mainCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job =
    GlobalScope.launch(mainDispatcher, coroutineStart) {
        block()
    }


fun <T> defaultCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job =
    GlobalScope.launch(defaultDispatcher, coroutineStart) {
        block()
    }


fun <T> unconfinedCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job =
    GlobalScope.launch(unconfinedDispatcher, coroutineStart) {
        block()
    }


suspend fun <T> withMainContext(block: suspend () -> T): T =
    withContext(mainDispatcher) {
        block()
    }


suspend fun <T> withIOContext(block: suspend () -> T): T =
    withContext(ioDispatcher) {
        block()
    }


suspend fun <T> withDefaultContext(block: suspend () -> T): T =
    withContext(defaultDispatcher) {
        block()
    }


suspend fun <T> withUnconfinedContext(block: suspend () -> T): T =
    withContext(Dispatchers.Unconfined) {
        block()
    }


suspend fun <T> withNonCancellableContext(block: suspend () -> T): T =
    withContext(NonCancellable) {
        block()
    }


/**
 *
 * @receiver ViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelIOCoroutine(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, action: suspend (scope: CoroutineScope) -> Unit = {}): Job =
    viewModelScope.launch(ioDispatcher, coroutineStart) {
        action(this)
    }


/**
 *
 * @receiver ViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelMainCoroutine(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, action: suspend (scope: CoroutineScope) -> Unit = {}): Job =
    viewModelScope.launch(mainDispatcher, coroutineStart) {
        action(this)
    }


/**
 *
 * @receiver ViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelDefaultCoroutine(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, action: suspend (scope: CoroutineScope) -> Unit = {}): Job =
    viewModelScope.launch(defaultDispatcher, coroutineStart) {
        action(this)
    }


/**
 *
 * @receiver ViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelUnconfinedCoroutine(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, action: suspend (scope: CoroutineScope) -> Unit = {}): Job =
    viewModelScope.launch(unconfinedDispatcher, coroutineStart) {
        action(this)
    }


/**
 *
 * @receiver ViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelNonCancellableCoroutine(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, action: suspend (scope: CoroutineScope) -> Unit = {}): Job =
    viewModelScope.launch(NonCancellable, coroutineStart) {
        action(this)
    }


inline fun CoroutineScope.main(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, crossinline function: suspend () -> Unit) {
    launch(mainDispatcher, coroutineStart) {
        function()
    }
}

inline fun CoroutineScope.io(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, crossinline function: suspend () -> Unit): Job =
    launch(ioDispatcher, coroutineStart) {
        function()
    }


inline fun CoroutineScope.default(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, crossinline function: suspend () -> Unit): Job =
    launch(defaultDispatcher, coroutineStart) {
        function()
    }


inline fun CoroutineScope.unconfined(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, crossinline function: suspend () -> Unit): Job =
    launch(unconfinedDispatcher, coroutineStart) {
        function()
    }


inline fun CoroutineScope.nonCancellable(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, crossinline function: suspend () -> Unit): Job =
    launch(NonCancellable, coroutineStart) {
        function()
    }


suspend fun ByteArray.toBitmapSuspend(): Bitmap? =
    onIO {
        return@onIO tryOrNull { BitmapFactory.decodeByteArray(this, 0, size) }
    }


internal inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    null
}

/**
 * Cancel the Job if it's active.
 */
fun Job?.cancelIfActive() {
    if (this?.isActive == true) {
        cancel()
    }
}


fun AppCompatActivity.ioCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(ioDispatcher) {
    action(this)
}


fun AppCompatActivity.mainCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(mainDispatcher) {
    action(this)
}


fun AppCompatActivity.unconfinedCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(unconfinedDispatcher) {
    action(this)
}


fun AppCompatActivity.defaultCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(defaultDispatcher) {
    action(this)
}


fun AppCompatActivity.nonCancellableCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(NonCancellable) {
    action(this)
}


fun Fragment.ioCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(ioDispatcher) {
    action(this)
}


fun Fragment.mainCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(mainDispatcher) {
    action(this)
}


fun Fragment.unconfinedCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(unconfinedDispatcher) {
    action(this)
}


fun Fragment.defaultCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(defaultDispatcher) {
    action(this)
}


fun Fragment.nonCancellableCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job = lifecycleScope.launch(NonCancellable) {
    action(this)
}

fun highPriorityContext() = Executors.newSingleThreadExecutor { runnable ->
    thread(priority = Thread.MAX_PRIORITY) { runnable.run() }
}.asCoroutineDispatcher()