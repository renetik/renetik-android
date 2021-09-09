package renetik.java.lang

import renetik.android.framework.common.catchAllWarn

fun <T> Class<T>.createInstance() = catchAllWarn { this.newInstance() }