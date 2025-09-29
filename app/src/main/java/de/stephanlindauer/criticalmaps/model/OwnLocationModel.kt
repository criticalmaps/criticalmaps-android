package de.stephanlindauer.criticalmaps.model

import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnLocationModel @Inject constructor() {

    @JvmField
    var ownLocation: GeoPoint? = null
    private var isLocationPrecise: Boolean = false

    fun setLocation(location: GeoPoint, accuracy: Float) {
        ownLocation = location
        isLocationPrecise = accuracy < ACCURACY_PRECISE_THRESHOLD
    }

    fun hasPreciseLocation(): Boolean {
        return ownLocation != null && isLocationPrecise
    }

    fun getLocationJson(): JSONObject {
        val locationObject = JSONObject()
        try {
            val location = ownLocation ?: throw NullPointerException("ownLocation is null")
            locationObject.put("longitude", (location.longitude * 1000000.0).toInt().toString())
            locationObject.put("latitude", (location.latitude * 1000000.0).toInt().toString())
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return locationObject
    }

    companion object {
        private const val ACCURACY_PRECISE_THRESHOLD = 50.0f // meters
    }
}