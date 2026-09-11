package renetik.android.controller.base

import android.content.Intent
import android.view.View
import android.view.ViewParent
import androidx.annotation.IdRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import renetik.android.core.lang.Fun

val CSActivityView<*>.intent: Intent get() = activity().intent

fun <T : View, ViewController : CSActivityView<T>>
        ViewController.push() = apply { navigation!!.push(this) }

fun <T : View, ViewController : CSActivityView<T>>
        ViewController.pushMain() = push("mainController")

fun <T : View, ViewController : CSActivityView<T>>
        ViewController.pushAsLast() = apply { navigation!!.pushAsLast(this) }

fun <T : View, ViewController : CSActivityView<T>>
        ViewController.push(pushKey: String) =
    apply { navigation!!.push(pushKey, this) }

var <T : View> CSActivityView<T>.requestedOrientation
    get() = activity().requestedOrientation
    set(value) {
        activity().requestedOrientation = value
    }

fun ViewParent.asActivityView() = ((this as? View)?.tag as? CSActivityView<*>)
fun View.asActivityView() = (this.tag as? CSActivityView<*>)

fun CSActivityView<*>.enterFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(activity().window, false)
    val controller = WindowInsetsControllerCompat(activity().window, view)
    controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller.hide(WindowInsetsCompat.Type.systemBars())
}

fun CSActivityView<*>.exitFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(activity().window, true)
    WindowInsetsControllerCompat(activity().window, view).show(WindowInsetsCompat.Type.systemBars())
}

fun CSActivityView<*>.onResume(listener: Fun) = eventResume.listen { listener() }
fun CSActivityView<*>.onPause(listener: Fun) = eventPause.listen { listener() }

val CSActivityView<*>.isPaused get() = !isResume

fun <T : View> CSActivityView<*>.activityView(
    @IdRes viewId: Int, init: (CSActivityView<T>).() -> Unit
): CSActivityView<T> = object : CSActivityView<T>(this, viewId) {
    init {
        init()
    }
}