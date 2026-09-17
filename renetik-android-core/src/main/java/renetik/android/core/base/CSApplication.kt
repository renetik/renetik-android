package renetik.android.core.base

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import renetik.android.core.android.content.CSToast.toast
import renetik.android.core.kotlin.className
import renetik.android.core.kotlin.findCause
import renetik.android.core.kotlin.primitives.isTrue
import renetik.android.core.lang.CSEnvironment
import renetik.android.core.lang.CSLang.ExitStatus.Error
import renetik.android.core.lang.CSLang.ExitStatus.OK
import renetik.android.core.lang.CSLang.exit
import renetik.android.core.lang.result.createSupervisorChild
import renetik.android.core.lang.result.named
import renetik.android.core.lang.variable.CSWeakVariable.Companion.weak
import renetik.android.core.logging.CSLog.logError
import renetik.android.core.logging.CSLog.logInfo
import renetik.android.core.logging.CSLog.logWarn
import java.lang.Runtime.getRuntime
import java.lang.Thread.sleep
import kotlin.math.max
import kotlin.reflect.KClass

abstract class CSApplication<ActivityType : AppCompatActivity> : Application(),
    ActivityLifecycleCallbacks {

    companion object {
        val app get() = CSEnvironment.app as CSApplication<*>

        fun getString(@StringRes resId: Int): String = app.languageContext.getString(resId)

        fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String =
            app.languageContext.getString(resId, *formatArgs)

        init {
            System.setProperty("kotlinx.coroutines.debug", "on")
        }
    }

    val scope = MainScope()
    var mainScope = createMainScope()
    protected fun createMainScope() = scope.createSupervisorChild()

    private val cores = getRuntime().availableProcessors()
    val Default = Dispatchers.Default.limitedParallelism(max(1, cores - 1))
        .named("$className Default")
    val IO = Dispatchers.IO.limitedParallelism(5)
        .named("$className IO")

    override fun onCreate() {
        super.onCreate()
        CSEnvironment.app = this
        registerDefaultUncaughtExceptionHandler()
        registerActivityLifecycleCallbacks(this)
    }

    private fun registerDefaultUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            when {
                throwable.isSystemDead() -> logWarn("Ignored dead system exception")
                throwable.isNotAttachedError() ->
                    logError(throwable, "Ignored window removal exception")

                throwable.isIncrementalInstallMissingResource() -> {
                    toast("App installation is corrupted."); sleep(500); exit(Error)
                }

                else -> defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun Throwable.isSystemDead(): Boolean = findCause {
        (it is android.os.DeadSystemException || it::class.java.simpleName == "DeadSystemRuntimeException")
    }

    private fun Throwable.isNotAttachedError(): Boolean = findCause {
        (it is IllegalArgumentException && message?.contains(
            "not attached to window manager").isTrue) || (it is WindowManager.BadTokenException)
    }

    private fun Throwable.isIncrementalInstallMissingResource(): Boolean = findCause {
        (it as? Resources.NotFoundException)?.message.let { message ->
            message?.contains("not fully present", ignoreCase = true).isTrue ||
                    message?.contains("file not fully present", ignoreCase = true).isTrue ||
                    message?.contains("incremental", ignoreCase = true).isTrue
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        logWarn { "onLowMemory" }
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val levelName = when (level) {
            TRIM_MEMORY_COMPLETE -> "TRIM_MEMORY_COMPLETE (app in background, system critically low)"
            TRIM_MEMORY_MODERATE -> "TRIM_MEMORY_MODERATE (app in background, system moderately low)"
            TRIM_MEMORY_BACKGROUND -> "TRIM_MEMORY_BACKGROUND (app in background, can release memory)"
            TRIM_MEMORY_UI_HIDDEN -> "TRIM_MEMORY_UI_HIDDEN (UI hidden, release UI resources)"
            TRIM_MEMORY_RUNNING_CRITICAL -> "TRIM_MEMORY_RUNNING_CRITICAL (foreground, system critically low)"
            TRIM_MEMORY_RUNNING_LOW -> "TRIM_MEMORY_RUNNING_LOW (foreground, system low)"
            TRIM_MEMORY_RUNNING_MODERATE -> "TRIM_MEMORY_RUNNING_MODERATE (foreground, system moderately low)"
            else -> "UNKNOWN ($level)"
        }
        logWarn { "onTrimMemory $levelName" }
    }

    override fun onTerminate() {
        super.onTerminate()
        logInfo { "onTerminate" }
    }

    abstract val activityClass: KClass<out ActivityType>

    var activity: ActivityType? by weak()
        protected set

    final override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (!activityClass.isInstance(activity)) return
        if (this.activity?.isDestroyed == false || this.activity?.isFinishing == false)
            logError("activity should be destroyed or null, " +
                    "when new is created, in single activity application")
        @Suppress("UNCHECKED_CAST") (activity as ActivityType)
            .also { this.activity = it; onActivityCreated(it) }
    }

    open fun onActivityCreated(activity: ActivityType) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (!activityClass.isInstance(activity)) return
        if (this.activity == activity) this.activity = null
    }

    open fun exit() {
        logInfo("Application Exit")
        exit(OK)
    }

    open val languageContext get():Context = this
}
