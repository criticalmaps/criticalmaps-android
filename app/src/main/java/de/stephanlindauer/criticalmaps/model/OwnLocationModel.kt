package de.stephanlindauer.criticalmaps.model

import org.json.JSONException
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnLocationModel @Inject constructor() {

    @JvmField
    var ownLocation: LatLng? = null
    private var isLocationPrecise = false

    fun setLocation(location: LatLng, accuracy: Float) {
        ownLocation = location
        isLocationPrecise = accuracy < ACCURACY_PRECISE_THRESHOLD
    }

    fun hasPreciseLocation(): Boolean = ownLocation != null && isLocationPrecise

    fun getLocationJson(): JSONObject {
        val location = ownLocation
        require(location != null) { "Location must be set before calling getLocationJson()" }

        val locationObject = JSONObject()
        try {
            locationObject.put("longitude", (location.longitude * 1_000_000.0).toInt().toString())
            locationObject.put("latitude", (location.latitude * 1_000_000.0).toInt().toString())
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return locationObject
    }

    companion object {
        private const val ACCURACY_PRECISE_THRESHOLD = 50.0f // meters
    }
}
