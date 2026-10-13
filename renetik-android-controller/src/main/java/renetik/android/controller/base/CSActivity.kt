package renetik.android.controller.base

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import renetik.android.core.android.content.isPermissionsGranted
import renetik.android.core.kotlin.className
import renetik.android.core.logging.CSLog.logInfo
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.android.CSHasContext
import renetik.android.event.fire
import renetik.android.event.lifecycle.destruct
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.ui.protocol.CSViewInterface
import renetik.android.ui.protocol.CSVisibility

abstract class CSActivity : AppCompatActivity(), CSVisibility, CSHasContext, CSViewInterface {
    val onActivityResult = event<CSActivityResult>()

    //CSVisibility
    override val isVisibility = property(true)

    //CSViewInterface
    override val view: View get() = window.decorView

    //CSHasContext
    override val context: Context get() = this

    override fun recreate() {
        destruct()
        super.recreate()
    }

    final override val registrations = CSRegistrationsMap(className)
    final override val eventDestruct = event<Unit>()
    override val isDestructed: Boolean get() = registrations.isCanceled
    override fun onDestruct() {
        registrations.cancel()
        eventDestruct.fire().clear()
        logInfo()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (!isDestructed) destruct()
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResult.fire(CSActivityResult(requestCode, resultCode, data))
        super.onActivityResult(requestCode, resultCode, data)
        logInfo("$requestCode $resultCode $data")
    }

    private var permissionsResult: CompletableDeferred<Map<String, Boolean>>? = null
    private val permissionsLauncher = registerForActivityResult(RequestMultiplePermissions()) {
        permissionsResult?.complete(it)
    }
    private val permissionsRequestMutex = Mutex()

    // Queued because the system cancels a request started while another one is shown.
    // A cancelled request delivers its result synchronously inside launch, so the awaited
    // result has to exist before launching.
    suspend fun requestPermissions(
        permissions: List<String>, requireAll: Boolean = true,
    ): Boolean = permissionsRequestMutex.withLock {
        if (isPermissionsGranted(permissions)) return@withLock true
        val result = CompletableDeferred<Map<String, Boolean>>().also { permissionsResult = it }
        permissionsLauncher.launch(permissions.toTypedArray())
        result.await().isPermissionsGranted(requireAll)
    }
}
