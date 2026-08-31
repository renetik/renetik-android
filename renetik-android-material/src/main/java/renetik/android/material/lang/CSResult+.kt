package renetik.android.material.lang

import androidx.annotation.StringRes
import renetik.android.controller.base.CSView
import renetik.android.core.base.CSApplication.Companion.getString
import renetik.android.core.lang.result.CSResult
import renetik.android.material.controller.snackError

suspend fun <T> CSResult<T>.snackError(parent: CSView<*>, @StringRes message: Int) =
    ifFailure { parent.snackError(getString(message)) }