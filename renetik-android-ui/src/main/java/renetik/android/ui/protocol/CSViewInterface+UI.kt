package renetik.android.ui.protocol

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import renetik.android.core.lang.ArgFun
import renetik.android.event.change.invoke
import renetik.android.event.change.onChangeOnce
import renetik.android.event.change.wait
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.destruct
import renetik.android.event.lifecycle.onDestructed
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import renetik.android.event.registration.plus
import renetik.android.ui.CSDisplayCutout
import renetik.android.ui.view.checkBox
import renetik.android.ui.view.compound
import renetik.android.ui.view.datePicker
import renetik.android.ui.view.editText
import renetik.android.ui.view.findView
import renetik.android.ui.view.horizontalScroll
import renetik.android.ui.view.imageView
import renetik.android.ui.view.listView
import renetik.android.ui.view.onClick
import renetik.android.ui.view.onClickLaunch
import renetik.android.ui.view.onGlobalFocus
import renetik.android.ui.view.onGlobalLayoutChange
import renetik.android.ui.view.onHasSize
import renetik.android.ui.view.progress
import renetik.android.ui.view.radio
import renetik.android.ui.view.rectangleInWindow
import renetik.android.ui.view.removeFromSuperview
import renetik.android.ui.view.scrollView
import renetik.android.ui.view.seekBar
import renetik.android.ui.view.textView
import renetik.android.ui.view.timePicker
import renetik.android.ui.view.view
import renetik.android.ui.view.webView

fun <T : View> CSViewInterface.findView(@IdRes id: Int): T? = view.findView(id)

@JvmName("viewOfType")
inline fun <reified Type : View> CSViewInterface.view(
    @IdRes id: Int, noinline onClick: ((view: View) -> Unit)? = null
): Type = view.findViewById<Type>(id)!!.apply { onClick?.let { onClick { it(this) } } }

fun CSViewInterface.view(
    @IdRes id: Int, onClick: ((view: View) -> Unit)? = null
): View = view.view(id).apply { onClick?.let { onClick { it(this) } } }

fun CSViewInterface.onClickLaunch(
    parent: CSHasRegistrations, timeout: Int? = null, onClick: suspend (view: View) -> Unit
) = view.onClickLaunch(parent, timeout, onClick)

fun CSViewInterface.viewOrNull(@IdRes id: Int): View? = view.findView(id)

fun CSViewInterface.views(@IdRes vararg ids: Int): List<View> =
    mutableListOf<View>().apply { for (id in ids) add(view(id)) }

fun CSViewInterface.simpleView(@IdRes id: Int) = view.view(id)
fun CSViewInterface.editText(@IdRes id: Int): EditText = view.editText(id)

fun CSViewInterface.textView(@IdRes id: Int, onClick: ArgFun<View>? = null) =
    view.textView(id).apply { onClick?.let { onClick { it(this) } } }

fun CSViewInterface.textViewOrNull(@IdRes id: Int, onClick: ArgFun<View>? = null) =
    view.findView<TextView>(id)?.apply { onClick?.let { onClick { it(this) } } }

fun CSViewInterface.scrollView(@IdRes id: Int) = view.scrollView(id)
fun CSViewInterface.horizontalScroll(@IdRes id: Int) =
    view.horizontalScroll(id)

fun CSViewInterface.listView(@IdRes id: Int) = view.listView(id)
fun CSViewInterface.radio(@IdRes id: Int) = view.radio(id)

fun CSViewInterface.datePicker(@IdRes id: Int) = view.datePicker(id)

fun CSViewInterface.viewGroup(@IdRes id: Int) = view.view<ViewGroup>(id)
fun CSViewInterface.frame(@IdRes id: Int) = view.view<FrameLayout>(id)
fun CSViewInterface.drawer(@IdRes id: Int) = view.view<DrawerLayout>(id)
fun CSViewInterface.linear(@IdRes id: Int) = view.view<LinearLayout>(id)
fun CSViewInterface.group(@IdRes id: Int) = view.view<ViewGroup>(id)
fun CSViewInterface.spinner(@IdRes id: Int) = view.view<Spinner>(id)
fun CSViewInterface.search(@IdRes id: Int) = view.view<SearchView>(id)

fun CSViewInterface.compound(@IdRes id: Int): CompoundButton = view.compound(id)

fun CSViewInterface.checkBox(@IdRes id: Int) = view.checkBox(id)
fun CSViewInterface.timePicker(@IdRes id: Int) = view.timePicker(id)
fun CSViewInterface.webView(@IdRes id: Int) = view.webView(id)

fun CSViewInterface.imageView(
    @IdRes id: Int, onClick: ArgFun<View>? = null
) = view.imageView(id).apply { onClick?.let { onClick { it(this) } } }

fun CSViewInterface.seekBar(@IdRes id: Int) = view.seekBar(id)
fun CSViewInterface.progress(@IdRes id: Int) = view.progress(id)

fun <Type : CSViewInterface> Type.removeFromSuperview() = apply {
    if (!isDestructed) view.removeFromSuperview()
}

fun <Type : CSViewInterface> Type.removeOnDestruct() = apply {
    onDestructed { view.removeFromSuperview() }
}

fun CSViewInterface.registerAfterLayout(function: () -> Unit) {
    view.onGlobalLayoutChange.onChangeOnce(this, function)
}

suspend fun CSViewInterface.waitForGlobalLayout() = view.onGlobalLayoutChange.wait()

fun <Type> Type.onGlobalFocus(function: (View?, View?) -> Unit): CSRegistration
        where  Type : CSViewInterface =
    this + view.onGlobalFocus { old, new -> function(old, new) }

@Suppress("DEPRECATION")
fun CSViewInterface.onSystemUiVisibilityChangeListener(
    function: () -> Unit
): CSRegistration {
    view.setOnSystemUiVisibilityChangeListener { function() }
    return CSRegistration(onCancel = {
        view.setOnSystemUiVisibilityChangeListener(null)
    })
}

val CSViewInterface.displayCutout: CSDisplayCutout?
    get() = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        view.rootWindowInsets?.displayCutout?.let { CSDisplayCutout(it) }
    } else null)

fun CSViewInterface.destroyAndRemoveFromParentWhenDestroyed(parent: CSHasDestruct) {
    this + parent.eventDestruct {
        val parentGroup = (view.parent as? ViewGroup)
        if (parentGroup !is AdapterView<*>) parentGroup?.removeView(view)
        destruct()
    }
}

val CSViewInterface.leftMarginInWindow: Int get() = view.rectangleInWindow.left

/**
 * @return [CSRegistration] of this listener. The registration is self-cancelled
 * on first change and must NOT be added to [CSHasRegistrations].
 */
fun CSViewInterface.onHasSize(function: () -> Unit): CSRegistration? =
    view.onHasSize(this) { function() }