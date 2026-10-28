package renetik.android.core.android.content

import android.content.Context

val Context.windowProfile: CSWindowProfile
    get() = resources.configuration.run {
        CSWindowProfile(screenWidthDp, screenHeightDp, smallestScreenWidthDp)
    }

val Context.isPortrait get() = windowProfile.isPortrait
val Context.isLandscape get() = windowProfile.isLandscape
val Context.isTablet get() = windowProfile.isTablet
val Context.isPhone get() = windowProfile.isPhone
