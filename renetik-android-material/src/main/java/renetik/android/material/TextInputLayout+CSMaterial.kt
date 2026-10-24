package renetik.android.material

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import androidx.annotation.FontRes
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.internal.CheckableImageButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import renetik.android.core.kotlin.privateField
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire
import renetik.android.event.listen
import renetik.android.ui.R
import renetik.android.ui.view.get
import renetik.android.ui.view.gone
import renetik.android.ui.view.propertyWithTag
import renetik.android.ui.widget.onFocusChange
import renetik.android.ui.widget.onTextChange
import renetik.android.ui.widget.text
import renetik.android.ui.widget.typeface

val <T : TextInputLayout> T.startIconView: CheckableImageButton?
    get() = privateField("startIconView")

fun <T : TextInputLayout> T.onCheck(listener: () -> Unit): T = apply {
    propertyWithTag(R.id.ViewEventOnChangeTag) { event<Unit>() }.listen(listener)
}

@Suppress("UNCHECKED_CAST")
fun <T : TextInputLayout> T.fireCheck(): T = apply {
    (getTag(R.id.ViewEventOnChangeTag) as? CSEvent<Unit>)?.fire()
}

@SuppressLint("RestrictedApi")
fun <T : TextInputLayout> T.startIconCheckable(checkable: Boolean = true) = apply {
    isStartIconCheckable = checkable
    if (checkable) setStartIconOnClickListener { isChecked = !isChecked }
    else setStartIconOnClickListener(null)
}

var <T : TextInputLayout> T.isChecked
    @SuppressLint("RestrictedApi") get() =
        startIconView?.isChecked ?: false
    set(value) {
        @SuppressLint("RestrictedApi")
        startIconView?.isChecked = value
        fireCheck()
    }

val TextInputLayout.autoCompleteView get() = (editText as AutoCompleteTextView)

fun <T : TextInputLayout> T.dropdown(context: Context, @LayoutRes itemLayout: Int,
                                     items: List<Any>, filter: Boolean = false,
                                     onItemChange: ((Int) -> Unit)? = null) = apply {
    val adapter = if (filter) ArrayAdapter(context, itemLayout, items) else
        NotFilteringArrayAdapter(context, itemLayout, items)
    autoCompleteView.setAdapter(adapter)
    autoCompleteView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
        onItemChange?.invoke(position)
    }
}

class NotFilteringArrayAdapter<T>(context: Context, @LayoutRes resource: Int, val objects: List<T>)
    : ArrayAdapter<T>(context, resource, objects) {

    val notFilteringFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults().apply {
            values = objects
            count = objects.size
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) =
            notifyDataSetChanged()
    }

    override fun getFilter() = notFilteringFilter
}

val <T : TextInputLayout> T.eventClear
    get() = propertyWithTag(R.id.ViewEventOnClearTag) { event<Unit>() }

fun <T : TextInputLayout> T.onClear(listener: () -> Unit): T = apply {
    eventClear.listen(listener)
}

/** Shows or hides the password from the start icon, which leaves the end icon to [withClear]. */
@SuppressLint("PrivateResource")
fun <T : TextInputLayout> T.withPasswordToggle(): T = apply {
    val passwordEdit = editText!!
    fun showPassword(isShown: Boolean) {
        passwordEdit.transformationMethod =
            if (isShown) null else PasswordTransformationMethod.getInstance()
        passwordEdit.setSelection(passwordEdit.length())
        setStartIconDrawable(
            if (isShown) com.google.android.material.R.drawable.design_ic_visibility_off
            else com.google.android.material.R.drawable.design_ic_visibility)
    }
    showPassword(isShown = false)
    setStartIconOnClickListener {
        showPassword(isShown = passwordEdit.transformationMethod is PasswordTransformationMethod)
    }
}

@SuppressLint("PrivateResource")
fun <T : TextInputLayout> T.withClear(): T = apply {
    var endIconDrawableToRestore: Drawable? = null
    var isEndIconVisibleToRestore: Boolean? = null
    var endIconModeToRestore: Int? = null
    // The mode is set first in both directions, because setting it replaces the end icon drawable.
    fun restoreState() {
        endIconMode = endIconModeToRestore!!
        endIconModeToRestore = null
        endIconDrawable = endIconDrawableToRestore
        endIconDrawableToRestore = null
        isEndIconVisible = isEndIconVisibleToRestore!!
        isEndIconVisibleToRestore = null
        // Imposible to restore click listener if was set and
        // there was visual glitch if set to null
        setEndIconOnClickListener { editText!!.performClick() }
    }

    fun saveStateToRestore() {
        endIconDrawableToRestore = endIconDrawable
        isEndIconVisibleToRestore = isEndIconVisible
        endIconModeToRestore = endIconMode
    }

    fun showClearIcon() {
        endIconMode = END_ICON_CUSTOM
        setEndIconDrawable(androidx.appcompat.R.drawable.abc_ic_clear_material)
        isEndIconVisible = true
        setEndIconOnClickListener {
//            restoreState()
            text = ""
            eventClear.fire()
        }
    }

    fun isClearVisible() = isEndIconVisibleToRestore != null
    fun updateClearIcon() {
        if (text.isNotEmpty()) {
            if (!isClearVisible()) {
                saveStateToRestore()
                showClearIcon()
            }
        } else if (isClearVisible()) restoreState()
    }
    updateClearIcon()
    editText!!.doAfterTextChanged { updateClearIcon() }
}

fun <T : TextInputLayout> T.filters(vararg filters: InputFilter) = apply {
    editText!!.filters = filters
}

fun TextInputLayout.error(hint: String? = null) {
    isErrorEnabled = true
    if (hint == null) {
        error(" ")
        if (childCount == 2) this[1].gone()
    } else error = hint
}

fun <T : TextInputLayout> T.errorClear() = apply {
    error = null
    isErrorEnabled = false
}

val TextInputLayout.isError get() = error != null && isErrorEnabled

fun TextInputLayout.onChangeClearError() = apply {
    onTextChange { if (isError) errorClear() }
}

fun TextInputLayout.typeface(@FontRes font: Int) = apply {
    typeface = ResourcesCompat.getFont(context, font)
}

fun TextInputLayout.textTypeFace(@FontRes font: Int) = apply {
    editText!!.typeface(font)
}

fun TextInputLayout.text() = editText!!.text()
fun TextInputLayout.text(value: String) = editText!!.text(value)

var <T : TextInputLayout> T.text: String
    get() = editText!!.text()
    set(value) {
        editText!!.text(value)
    }

fun <T : TextInputLayout> T.title(string: String) = apply { text = string }

fun <T : TextInputLayout> T.onTextChange(onChange: (view: T) -> Unit) =
    apply { editText!!.onTextChange { onChange(this) } }

fun <T : TextInputLayout> T.onFocusChange(onChange: (view: T) -> Unit) =
    apply { editText!!.onFocusChange { onChange(this) } }