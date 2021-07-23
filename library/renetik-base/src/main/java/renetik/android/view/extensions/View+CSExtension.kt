package renetik.android.view.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.annotation.IdRes
import androidx.appcompat.widget.ContentFrameLayout
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import renetik.android.R
import renetik.android.framework.event.CSVisibleEventOwner
import renetik.android.framework.event.property.CSEventProperty
import renetik.android.framework.event.property.CSEventPropertyFunctions.property
import renetik.android.java.extensions.isNull
import renetik.android.primitives.isTrue
import renetik.android.view.adapter.CSClickAdapter

fun <T : View> View.findView(@IdRes id: Int): T? = findViewById(id)
fun View.view(@IdRes id: Int) = findView<View>(id)!!
fun View.editText(@IdRes id: Int) = findView<EditText>(id)!!
fun View.textView(@IdRes id: Int) = findView<TextView>(id)!!
fun View.scrollView(@IdRes id: Int) = findView<ScrollView>(id)!!
fun View.horizontalScroll(@IdRes id: Int) = findView<HorizontalScrollView>(id)!!
fun View.listView(@IdRes id: Int) = findView<ListView>(id)!!
fun View.radio(@IdRes id: Int) = findView<RadioButton>(id)!!
fun View.datePicker(@IdRes id: Int) = findView<DatePicker>(id)!!
fun View.numberPicker(@IdRes id: Int) = findView<NumberPicker>(id)!!
fun View.frame(@IdRes id: Int) = findView<FrameLayout>(id)!!
fun View.viewGroup(@IdRes id: Int) = findView<ViewGroup>(id)!!
fun View.linearLayout(@IdRes id: Int) = findView<LinearLayout>(id)!!
fun View.group(@IdRes id: Int) = findView<ViewGroup>(id)!!
fun View.spinner(@IdRes id: Int) = findView<Spinner>(id)!!
fun View.search(@IdRes id: Int) = findView<SearchView>(id)!!
fun View.button(@IdRes id: Int) = findView<Button>(id)!!
fun View.compound(@IdRes id: Int) = findView<CompoundButton>(id)!!
fun View.checkBox(@IdRes id: Int) = findView<CheckBox>(id)!!
fun View.switch(@IdRes id: Int) = findView<Switch>(id)!!
fun View.timePicker(@IdRes id: Int) = findView<TimePicker>(id)!!
fun View.webView(@IdRes id: Int) = findView<WebView>(id)!!
fun View.imageView(@IdRes id: Int) = findView<ImageView>(id)!!
fun View.swipeRefresh(@IdRes id: Int) = findView<SwipeRefreshLayout>(id)!!
fun View.seekBar(@IdRes id: Int) = findView<SeekBar>(id)!!
fun View.toolbar(@IdRes id: Int) = findView<Toolbar>(id)!!

fun <T : View> T.enabledIf(condition: Boolean) = apply { isEnabled = condition }

fun <T : View> T.disabledIf(condition: Boolean) = apply { isEnabled = !condition }

fun <T : View> T.enabled() = apply { isEnabled = true }

fun <T : View> T.disabled() = apply { isEnabled = false }

val <T : View> T.isVisible get() = visibility == VISIBLE

val <T : View> T.isInvisible get() = visibility == INVISIBLE

val <T : View> T.isGone get() = visibility == GONE

fun <T : View> T.show() = apply { visibility = VISIBLE }

fun <T : View> T.visible() = apply { visibility = VISIBLE }

fun <T : View> T.hide() = apply { visibility = GONE }

fun <T : View> T.gone() = apply { visibility = GONE }

fun <T : View> T.invisible() = apply { visibility = INVISIBLE }

fun <T : View> T.visibleIf(condition: Boolean) = apply { if (condition) visible() else invisible() }

fun <T : View> T.invisibleIf(condition: Boolean) =
    apply { if (condition) invisible() else visible() }

fun <T : View> T.shownIf(condition: Boolean?, fade: Boolean = false) = apply {
    when {
        fade -> if (condition.isTrue) fadeIn() else fadeOut()
        condition.isTrue -> show()
        else -> gone()
    }
}

fun <T : View> T.hiddenIf(condition: Boolean?) = apply { if (condition.isTrue) gone() else show() }

val <T : View> T.superview get() = parent as? View

val <T : View> T.parentView get() = parent as? View

fun <T : View> T.removeFromSuperview() = apply { (parent as? ViewGroup)?.remove(this) }

fun <T : View> View.findViewRecursive(id: Int): T? = findView(id)
    ?: parentView?.findViewRecursive(id)

fun <T : View> T.onClick(onClick: (view: T) -> Unit) =
    apply { setOnClickListener(CSClickAdapter { onClick(this) }) }

fun <T : View> T.createBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).apply {
        background?.draw(this) ?: this.drawColor(Color.WHITE)
        draw(this)
    }
    return bitmap
}

fun <T : Any> View.propertyWithTag(@IdRes key: Int, onCreate: () -> T): T {
    @Suppress("UNCHECKED_CAST")
    var value = getTag(key) as? T
    if (value.isNull) {
        value = onCreate()
        setTag(key, value)
    }
    return value!!
}

fun View.getRectangleOnScreen(location: IntArray, rectangle: Rect) {
    getLocationOnScreen(location)
    rectangle.set(location[0], location[1], location[0] + width, location[1] + height)
}

fun <T> View.modelProperty(): CSEventProperty<T?> =
    propertyWithTag(R.id.ViewModelTag) { property(null) }

fun <T> View.model(value: T?) = apply { modelProperty<T?>().value(value) }
fun <T> View.model(): T? = modelProperty<T?>().value

fun View.id(value: Int) = apply { id = value }

fun View.visibilityPropertySet(parent: CSVisibleEventOwner, property: CSEventProperty<Any?>) =
    apply {
        fun updateVisibility() = shownIf(property.value != null)
        parent.whileShowing(property.onChange { updateVisibility() })
        updateVisibility()
    }

fun View.visibilityPropertyTrue(parent: CSVisibleEventOwner, property: CSEventProperty<Boolean>) =
    apply {
        fun updateVisibility() = shownIf(property.value)
        parent.whileShowing(property.onChange { updateVisibility() })
        updateVisibility()
    }

fun <T> View.visibilityPropertyEquals(parent: CSVisibleEventOwner,
                                      property: CSEventProperty<T?>,
                                      value: T) = apply {
    fun updateVisibility() = shownIf(property.value == value)
    parent.whileShowing(property.onChange { updateVisibility() })
    updateVisibility()
}


// This had be done because isShown return false in on Resume
// for main activity view when created
// because it has not yet attached its DecorView.class to window
// DecorView is internal class so we cant identify it by class just className DecorView
// Other solution is to identify ContentFrameLayout instead as top view
// Previous simple "solution": view.parent?.parent?.parent?.parent != null
fun View.isShowing(): Boolean {
    if (!isVisible) return false
    var view: View = this
    while (true) {
        val parent = view.parent
        when {
            parent == null -> return false
            parent !is View -> return true
            parent is ContentFrameLayout -> return true
//            parent.className == "DecorView" -> return true
            !parent.isVisible -> return false
            else -> view = parent
        }
    }
}







