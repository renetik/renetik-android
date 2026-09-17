package renetik.android.material.lang

import androidx.annotation.StringRes
import renetik.android.controller.base.CSView
import renetik.android.core.base.CSApplication.Companion.getString
import renetik.android.core.lang.result.CSResult
import renetik.android.material.controller.snackError
import renetik.android.material.controller.snackInfo

suspend fun <T> CSResult<T>.snackSuccess(parent: CSView<*>, @StringRes message: Int) =
    ifSuccess { parent.snackInfo(getString(message)) }

suspend fun <T> CSResult<T>.snackSuccess(parent: CSView<*>, createMessage :(T) -> Int) =
    ifSuccess { parent.snackInfo(getString(createMessage(it))) }

suspend fun <T> CSResult<T>.snackError(parent: CSView<*>, @StringRes message: Int) =
    ifFailure { parent.snackError(getString(message)) }

suspend fun <T> CSResult<T>.snackError(parent: CSView<*>, createMessage :(CSResult<*>) -> Int) =
    ifFailure { parent.snackError(getString(createMessage(it))) }