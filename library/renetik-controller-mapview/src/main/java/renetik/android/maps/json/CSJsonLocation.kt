package renetik.android.maps.json

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import renetik.android.framework.store.getList
import renetik.android.java.extensions.collections.list
import renetik.android.json.data.CSJsonMapStore
import renetik.android.maps.extensions.asLatLng

@Suppress("unchecked_cast")
class CSJsonLocation(val data: CSJsonMapStore, private val key: String) {
    var latLng: LatLng?
        get() = (data.getList(key) as? List<Double>)?.let { LatLng(it[0], it[1]) }
        set(latLng) {
            data.save(key, list(latLng?.latitude, latLng?.longitude))
        }

    fun set(location: Location) {
        latLng = location.asLatLng()
    }

    val value get() = latLng!!

    override fun toString() = "$latLng"
}