package de.stephanlindauer.criticalmaps.model

import org.json.JSONArray
import org.json.JSONException
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtherUsersLocationModel @Inject constructor(
    private val userModel: UserModel
) {

    private var otherUsersLocations = ArrayList<GeoPoint>()

    @Throws(JSONException::class)
    fun setFromJson(jsonArray: JSONArray) {
        otherUsersLocations = ArrayList(jsonArray.length())
        for (i in 0 until jsonArray.length()) {
            val locationObject = jsonArray.getJSONObject(i)
            if (locationObject.getString("device") == userModel.changingDeviceToken) {
                continue // Ignore own location
            }
            val latitudeE6 = locationObject.getString("latitude").toInt()
            val longitudeE6 = locationObject.getString("longitude").toInt()

            otherUsersLocations.add(
                GeoPoint(latitudeE6 / 1000000.0, longitudeE6 / 1000000.0)
            )
        }
    }

    fun getOtherUsersLocations(): ArrayList<GeoPoint> {
        return otherUsersLocations
    }
}