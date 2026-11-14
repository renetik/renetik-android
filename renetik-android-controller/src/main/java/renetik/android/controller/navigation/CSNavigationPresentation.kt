package renetik.android.controller.navigation

import android.view.View
import renetik.android.controller.navigation.CSContentSize.Companion.Layout
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Bottom

sealed interface CSNavigationPresentation {

    /** Positioned next to [anchor], dismissed by touching outside. */
    data class Popup(
        val anchor: View,
        val side: CSNavigationItemPopupSide = Bottom,
        val size: CSContentSize = Layout,
    ) : CSNavigationPresentation

    /** Centered and shrunk to fit the screen. */
    data class Window(val size: CSContentSize = Layout) : CSNavigationPresentation

    data object FullScreen : CSNavigationPresentation
}
