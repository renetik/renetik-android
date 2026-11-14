package renetik.android.controller.navigation

import androidx.annotation.DimenRes

/** Size of a navigation item's content; a null dimension keeps the layout root's own size. */
data class CSContentSize(
    @DimenRes val width: Int? = null,
    @DimenRes val height: Int? = null,
) {
    fun overriddenBy(size: CSContentSize) =
        CSContentSize(size.width ?: width, size.height ?: height)

    companion object {
        val Layout = CSContentSize()
    }
}
