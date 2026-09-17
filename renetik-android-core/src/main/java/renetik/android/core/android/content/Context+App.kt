package renetik.android.core.android.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import renetik.android.core.kotlin.primitives.isFlagSet
import renetik.android.core.kotlin.logWarn

val Context.isDebug get() = applicationInfo.flags isFlagSet FLAG_DEBUGGABLE

/**
 * If the item does not have an icon, the item's default icon is returned
 * such as the default activity icon.
 */
val Context.applicationIcon: Drawable get() = applicationInfo.loadIcon(packageManager)
val Context.applicationLabel: String get() = "${applicationInfo.loadLabel(packageManager)}"
val Context.applicationLogo: Drawable? get() = applicationInfo.loadLogo(packageManager)

@Suppress("DEPRECATION")
val Context.packageVersionString
    get() = packageInfo!!.versionCode.toString() + "-" + packageInfo!!.versionName

@Suppress("DEPRECATION")
val Context.packageVersionCode
    get() = packageInfo!!.versionCode

val Context.packageInfo
    get() = runCatching<PackageInfo> {
        packageManager.getPackageInfo(packageName, 0)
    }.logWarn().getOrNull()

val Context.isInitialInstalledVersion: Boolean
    get() = runCatching {
        packageManager.getPackageInfo(packageName, 0).let {
            it.firstInstallTime == it.lastUpdateTime
        }
    }.getOrDefault(true)

val Context.isPlayStoreInstalled get() = isPackageInstalled("com.android.vending")

fun Context.isPackageInstalled(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, 0)
    true
} catch (e: NameNotFoundException) {
    false
}

fun Context.goHome() =
    startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))

fun Context.startApplication(packageName: String) {
    try {
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.LAUNCHER")
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        for (info in resolveInfoList)
            if (info.activityInfo.packageName.equals(packageName, ignoreCase = true)) {
                launchComponent(info.activityInfo.packageName, info.activityInfo.name)
                return
            }
        showInMarket(packageName)
    } catch (e: Exception) {
        showInMarket(packageName)
    }
}

private fun Context.launchComponent(packageName: String, name: String) {
    val intent = Intent("android.intent.action.MAIN")
    intent.addCategory("android.intent.category.LAUNCHER")
    intent.component = ComponentName(packageName, name)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

private fun Context.showInMarket(packageName: String?) =
    openUrl("market://details?id=" + packageName!!)
