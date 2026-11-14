package renetik.android.controller.navigation

import android.view.View
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Bottom
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Left
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Right
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Top
import renetik.android.ui.view.bottomFloat
import renetik.android.ui.view.locationInWindow
import renetik.android.ui.view.rightFloat

/** Places a popup frame next to its anchor, inside [area] inset by the paddings. */
class CSNavigationItemPopupPlacement(
    private val area: View,
    private val horizontalPadding: Float,
    private val verticalPadding: Float,
) {
    private val right get() = area.width - horizontalPadding
    private val bottom get() = area.height - verticalPadding

    fun place(frame: View, anchor: View, side: CSNavigationItemPopupSide) {
        val anchorLocation = anchor.locationInWindow
        when (side) {
            Bottom -> {
                frame.x = anchorCenteredX(frame, anchor, anchorLocation.x)
                frame.y = anchorLocation.y.toFloat() + anchor.height
            }
            Top -> {
                frame.x = anchorCenteredX(frame, anchor, anchorLocation.x)
                frame.y = anchorLocation.y.toFloat() - frame.height - verticalPadding
            }
            Right -> {
                frame.x = anchorLocation.x.toFloat() + anchor.width
                frame.y = anchorCenteredY(frame, anchor, anchorLocation.y)
            }
            Left -> {
                frame.x = anchorLocation.x.toFloat() - frame.width
                frame.y = anchorLocation.y.toFloat() - verticalPadding
            }
        }
        keepInside(frame)
    }

    fun keepInside(frame: View) {
        if (frame.bottomFloat > bottom) frame.bottomFloat = bottom
        if (frame.rightFloat > right) frame.rightFloat = right
    }

    private fun anchorCenteredX(frame: View, anchor: View, anchorX: Int) =
        (anchorX + (anchor.width - frame.width) / 2f)
            .coerceAtMost(right - frame.width).coerceAtLeast(horizontalPadding)

    private fun anchorCenteredY(frame: View, anchor: View, anchorY: Int) =
        (anchorY + (anchor.height - frame.height) / 2f)
            .coerceAtMost(bottom - frame.height).coerceAtLeast(verticalPadding)
}
