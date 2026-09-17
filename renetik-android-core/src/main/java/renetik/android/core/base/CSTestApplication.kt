package renetik.android.core.base

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.cancel

class CSTestApplication : CSApplication<AppCompatActivity>() {
    override val activityClass = AppCompatActivity::class

    override fun onTerminate() {
        scope.cancel()
        super.onTerminate()
    }
}
