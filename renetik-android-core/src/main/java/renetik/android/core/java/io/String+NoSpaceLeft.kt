package renetik.android.core.java.io

/** Whether this error text gives the system's reason for a write to a full device (ENOSPC). */
val String.reportsNoSpaceLeft: Boolean get() = "No space left on device" in this
