package renetik.android.material.lang

import androidx.annotation.StringRes
import renetik.android.controller.base.CSView
import renetik.android.core.base.CSApplication.Companion.getString
import renetik.android.material.controller.snackError
import renetik.android.material.controller.snackInfo

fun <T> Result<T>.snackSuccess(parent: CSView<*>, @StringRes message: Int) =
    onSuccess { parent.snackInfo(getString(message)) }

fun <T> Result<T>.snackError(parent: CSView<*>, @StringRes message: Int) =
    onFailure { parent.snackError(getString(message)) }