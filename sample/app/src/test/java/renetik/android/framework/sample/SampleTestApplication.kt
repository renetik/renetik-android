package renetik.android.framework.sample

import kotlinx.coroutines.cancel

class SampleTestApplication : SampleApplication() {
    override fun onTerminate() {
        scope.cancel()
        super.onTerminate()
    }
}
