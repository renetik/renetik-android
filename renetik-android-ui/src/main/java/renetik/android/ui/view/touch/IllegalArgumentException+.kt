package renetik.android.ui.view.touch

import renetik.android.core.logging.CSLog.logWarn

/**
 * Android's touch handling throws for an invalid pointerIndex on some multi-touch sequences, a
 * framework bug that is only logged; any other exception is rethrown.
 */
fun IllegalArgumentException.logPointerIndexBugOrThrow() {
    if (message?.contains("pointerIndex", ignoreCase = true) != true) throw this
    logWarn(message)
}
