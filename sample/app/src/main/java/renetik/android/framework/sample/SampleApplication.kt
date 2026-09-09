package renetik.android.framework.sample

import renetik.android.core.base.CSApplication

open class SampleApplication : CSApplication<MainActivity>() {
    override val activityClass = MainActivity::class

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
    }
}
