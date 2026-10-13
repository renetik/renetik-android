package renetik.android.controller.base

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions

// Empty results mean the system cancelled the request, so they count as denied.
// Without requireAll a partial grant is enough, like approximate instead of precise location.
fun Map<String, Boolean>.isPermissionsGranted(requireAll: Boolean = true): Boolean =
    isNotEmpty() && if (requireAll) values.all { it } else values.any { it }

// Has to be registered while the fragment or activity is constructed, the returned request can
// be invoked later, its result is delivered even when the activity was recreated meanwhile.
fun ActivityResultCaller.permissionRequest(
    vararg permissions: String, requireAll: Boolean = true,
    onDenied: (() -> Unit)? = null, onGranted: () -> Unit,
): () -> Unit {
    val requested = arrayOf(*permissions)
    val launcher = registerForActivityResult(RequestMultiplePermissions()) { results ->
        if (results.isPermissionsGranted(requireAll)) onGranted() else onDenied?.invoke()
    }
    return { launcher.launch(requested) }
}
