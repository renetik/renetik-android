package renetik.android.controller.base

import android.app.Activity.RESULT_OK
import android.content.Intent

class CSActivityResult(val requestCode: Int, val resultCode: Int, val data: Intent?) {

    fun conformTo(requestCode: Int, resulCode: Int) =
            this.requestCode == requestCode && this.resultCode == resulCode

    fun isOK(requestCode: Int) = conformTo(requestCode, RESULT_OK)

    fun isOK() = resultCode == RESULT_OK
}