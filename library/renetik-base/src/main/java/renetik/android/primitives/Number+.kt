package renetik.android.primitives

import renetik.android.framework.lang.CSComparisionConstants.Ascending
import renetik.android.framework.lang.CSComparisionConstants.Descending
import renetik.android.framework.lang.CSComparisionConstants.Equal
import renetik.android.java.extensions.isNull
import renetik.android.java.extensions.notNull

fun compare(x: Int?, y: Int?): Int {
    if (x.notNull && y.notNull) return x!!.compareTo(y!!)
    if (x.isNull && y.isNull) return Equal
    return if (x.isNull && y.notNull) Ascending else Descending
}

fun compare(x: Float?, y: Float?): Int {
    if (x.notNull && y.notNull) return x!!.compareTo(y!!)
    if (x.isNull && y.isNull) return 0
    return if (x.isNull && y.notNull) Ascending else Descending
}