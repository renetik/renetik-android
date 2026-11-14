package renetik.android.controller.navigation

import android.view.View
import renetik.android.controller.base.CSView
import renetik.android.controller.navigation.CSNavigationItemAnimation.Fade
import renetik.android.controller.navigation.CSNavigationItemAnimation.None
import renetik.android.controller.navigation.CSNavigationPresentation.FullScreen
import renetik.android.controller.navigation.CSNavigationPresentation.Window
import renetik.android.core.lang.CSLeakCanary

fun <T : CSNavigationItemView> T.selected(button: CSView<*>) = selected(button.view)

fun <T : CSNavigationItemView> T.selected(button: View) = apply {
    button.isSelected = true
    onClose { button.isSelected = false }
}

fun <T : CSNavigationItemView> T.show(
    animation: CSNavigationItemAnimation = Fade
) = apply {
    this.animation = if (CSLeakCanary.isEnabled) None else animation
    navigation!!.push(this)
    updateVisibility()
}

fun <T : CSNavigationItemView> T.show(
    presentation: CSNavigationPresentation,
    animation: CSNavigationItemAnimation = Fade
) = apply {
    present(presentation)
    show(animation)
}

fun <T : CSNavigationItemView> T.center() = apply { present(Window()) }

fun <T : CSNavigationItemView> T.fullScreen() = apply { present(FullScreen) }

val <T : CSNavigationItemView> T.isClicksBlocked get() = view.isClickable
