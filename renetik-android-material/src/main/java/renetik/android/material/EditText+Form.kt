package renetik.android.material

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.widget.EditText
import androidx.appcompat.R.drawable.abc_ic_clear_material
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import renetik.android.ui.view.parentView
import renetik.android.ui.widget.clearText
import renetik.android.ui.widget.drawableStart
import renetik.android.ui.widget.onFocusChange

val EditText.inputLayout get() = (parentView?.parentView as? TextInputLayout)

fun <T : EditText> T.withStartIcon(icon: Int, onClick: () -> Unit) = apply {
    inputLayout?.setStartIconDrawable(icon)
    inputLayout?.setStartIconOnClickListener { onClick() }
}

fun <T : EditText> T.withEndIcon(icon: Int, onClick: () -> Unit) = apply {
    inputLayout?.setEndIconDrawable(icon)
    inputLayout?.setEndIconOnClickListener { onClick() }
}

fun <T : EditText> T.withStartIconClear(onClear: () -> Unit) = apply {
    val layout = inputLayout
    if (layout != null) withLayoutClearIcon(layout, onClear)
    else withCompoundClearIcon(onClear)
}

fun <T : EditText> T.withStartClearText() = withStartIconClear { clearText() }

private fun EditText.withLayoutClearIcon(layout: TextInputLayout, onClear: () -> Unit) {
    var isShown: Boolean? = null
    fun updateClearIcon() {
        val show = text.isNotBlank()
        if (show == isShown) return
        isShown = show
        if (show) withStartIcon(abc_ic_clear_material, onClear)
        else layout.setStartIconDrawable(null)
    }
    updateClearIcon()
    onFocusChange { updateClearIcon() }
    doAfterTextChanged { updateClearIcon() }
}

@SuppressLint("ClickableViewAccessibility")
private fun EditText.withCompoundClearIcon(onClear: () -> Unit) {
    var isShown: Boolean? = null
    fun updateClearIcon() {
        val show = text.isNotBlank()
        if (show == isShown) return
        isShown = show
        drawableStart(if (show) abc_ic_clear_material else null)
    }
    updateClearIcon()
    doAfterTextChanged { updateClearIcon() }
    setOnTouchListener { _, event ->
        if (event.action == ACTION_UP && isClearIconTouch(event)) {
            onClear()
            true
        } else false
    }
}

// Compound drawable is drawn vertically centered, without this vertical bound
// the whole left edge of multi line EditText would clear its text.
private fun EditText.isClearIconTouch(event: MotionEvent): Boolean {
    val iconHeight = compoundDrawables[0]?.bounds?.height() ?: return false
    val iconTop = paddingTop + (height - paddingTop - paddingBottom - iconHeight) / 2f
    return event.x <= compoundPaddingLeft
            && event.y >= iconTop && event.y <= iconTop + iconHeight
}
