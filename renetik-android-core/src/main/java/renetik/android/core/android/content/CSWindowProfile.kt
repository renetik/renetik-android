package renetik.android.core.android.content

data class CSWindowProfile(val widthDp: Int, val heightDp: Int, val smallestWidthDp: Int) {
    val isPortrait: Boolean get() = heightDp >= widthDp
    val isLandscape: Boolean get() = !isPortrait
    val isTablet: Boolean get() = smallestWidthDp >= 600
    val isPhone: Boolean get() = !isTablet

    // From targetSdk 35 the configuration includes system bars, so the smallest width is the
    // landscape height. The portrait height estimates the landscape width, a display cutout
    // can differ per orientation. Below targetSdk 35 both overestimate by the system bars.
    val landscape: CSWindowProfile
        get() = if (isLandscape) this
        else copy(widthDp = heightDp, heightDp = smallestWidthDp)
}
