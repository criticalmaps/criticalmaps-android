package de.stephanlindauer.criticalmaps.model

import org.json.JSONArray
import org.json.JSONException
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtherUsersLocationModel @Inject constructor(
    private val userModel: UserModel
) {
    private val otherUsersLocations = HashMap<String, LatLng>()

    @Throws(JSONException::class)
    fun setFromJson(jsonArray: JSONArray) {
        for (i in 0 until jsonArray.length()) {
            val locationObject = jsonArray.getJSONObject(i)
            val deviceId = locationObject.getString("device")

            // Ignore own location
            if (deviceId == userModel.changingDeviceToken) {
                continue
            }

            val latitudeE6 = locationObject.getString("latitude").toInt()
            val longitudeE6 = locationObject.getString("longitude").toInt()

            otherUsersLocations[deviceId] = LatLng(latitudeE6 / 1E6, longitudeE6 / 1E6)
        }
    }

    fun getOtherUsersLocations(): Map<String, LatLng> = otherUsersLocations
}
