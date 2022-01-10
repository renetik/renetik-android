package renetik.android.controller.base

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.drawerlayout.widget.DrawerLayout
import renetik.android.framework.event.*
import renetik.android.view.*
import renetik.android.widget.onChange
import renetik.android.widget.radioGroup

fun <T : View> CSViewInterface.findView(@IdRes id: Int): T? = view.findView(id)

fun CSViewInterface.view(@IdRes id: Int, onClick: ((view: View) -> Unit)? = null) =
    view.view(id).apply { onClick?.let { this.onClick(it) } }

fun CSViewInterface.views(@IdRes vararg ids: Int): List<View> =
    mutableListOf<View>().apply { for (id in ids) add(view(id)) }

fun CSViewInterface.simpleView(@IdRes id: Int) = view.view(id)
fun CSViewInterface.editText(@IdRes id: Int) = view.editText(id)
fun CSViewInterface.textView(@IdRes id: Int, onClick: ((view: View) -> Unit)? = null) =
    view.textView(id).apply { onClick?.let { this.onClick(it) } }

fun CSViewInterface.textViewOrNull(@IdRes id: Int, onClick: ((view: View) -> Unit)? = null) =
    view.findView<TextView>(id)?.apply { onClick?.let { this.onClick(it) } }

fun CSViewInterface.scrollView(@IdRes id: Int) = view.scrollView(id)
fun CSViewInterface.horizontalScroll(@IdRes id: Int) =
    view.horizontalScroll(id)

fun CSViewInterface.listView(@IdRes id: Int) = view.listView(id)
fun CSViewInterface.radio(@IdRes id: Int) = view.radio(id)

fun CSViewInterface.datePicker(@IdRes id: Int) = view.datePicker(id)

//fun CSViewInterface.numberPicker(@IdRes id: Int) = view.numberPicker(id)
fun CSViewInterface.viewGroup(@IdRes id: Int) = view.viewGroup(id)
fun CSViewInterface.frame(@IdRes id: Int) = view.frame(id)
fun CSViewInterface.drawer(@IdRes id: Int) = view.view(id) as DrawerLayout
fun CSViewInterface.linear(@IdRes id: Int) = view.linear(id)
fun CSViewInterface.group(@IdRes id: Int) = view.group(id)
fun CSViewInterface.spinner(@IdRes id: Int) = view.spinner(id)
fun CSViewInterface.search(@IdRes id: Int) = view.search(id)
fun CSViewInterface.button(@IdRes id: Int,
                           onClick: ((view: Button) -> Unit)? = null) =
    view.button(id).apply { onClick?.let { this.onClick(it) } }

fun CSViewInterface.compound(@IdRes id: Int) = view.compound(id)

//fun CSView<*>.switch(@IdRes id: Int) = view.switch(id)
fun CSViewInterface.checkBox(@IdRes id: Int) = view.checkBox(id)
fun CSViewInterface.timePicker(@IdRes id: Int) = view.timePicker(id)
fun CSViewInterface.webView(@IdRes id: Int) = view.webView(id)
fun CSViewInterface.imageView(@IdRes id: Int,
                              onClick: ((view: ImageView) -> Unit)? = null) =
    view.imageView(id).apply { onClick?.let { this.onClick(it) } }

fun CSViewInterface.swipeRefresh(@IdRes id: Int) = view.swipeRefresh(id)
fun CSViewInterface.seekBar(@IdRes id: Int) = view.seekBar(id)
fun CSViewInterface.progress(@IdRes id: Int) = view.progress(id)
fun CSViewInterface.radioGroup(@IdRes id: Int,
                               onChange: ((buttonId: Int) -> Unit)? = null): RadioGroup =
    view.radioGroup(id).apply { onChange?.let { this.onChange(it) } }

fun CSView<*>.inflateView(layoutId: Int) = inflate<View>(layoutId)

fun <Type : CSView<*>> Type.removeFromSuperview() = apply {
    view.removeFromSuperview()
}

fun <Type> Type.afterGlobalLayout(function: (Type) -> Unit): CSEventRegistration
        where  Type : CSView<*>, Type : CSEventOwner {
    lateinit var registration: CSEventRegistration
    registration = register(view.afterGlobalLayout {
        function(this)
        remove(registration)
    })
    return registration
}

fun <Type> Type.onGlobalFocus(function: (View?, View?) -> Unit)
        where  Type : CSView<*>, Type : CSEventOwner =
    register(view.onGlobalFocus { old, new -> function(old, new) })

fun <Type> Type.later(delayMilliseconds: Int = 0, function: () -> Unit)
        where  Type : CSView<*>, Type : CSEventOwner = apply {
    lateinit var registration: CSEventRegistration
    registration = register(renetik.kotlin.later(delayMilliseconds) {
        function()
        remove(registration)
    })
}

fun <Type> Type.later(function: () -> Unit)
        where  Type : CSView<*>, Type : CSEventOwner = later(0, function)

fun <Type> Type.hasSize(function: (Type) -> Unit)
        where  Type : CSView<*>, Type : CSEventOwner =
    apply {
        var registration: CSEventRegistration? = null
        registration = register(view.hasSize {
            function(this)
            remove(registration)
        })
    }

fun View.asCSView() = asCS<CSView<*>>()
fun View.asCSActivityView() = asCS<CSActivityView<*>>()
fun <CSViewType : CSView<*>> View.asCS() = ((this as? View)?.tag as? CSViewType)

