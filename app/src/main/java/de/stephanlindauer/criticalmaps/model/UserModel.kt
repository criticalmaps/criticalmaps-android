package de.stephanlindauer.criticalmaps.model

import android.annotation.SuppressLint
import android.provider.Settings
import de.stephanlindauer.criticalmaps.App
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserModel @Inject constructor(app: App) {

    @SuppressLint("HardwareIds")
    val changingDeviceToken: String

    init {
        val androidId = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)

        val gregorianCalendar = GregorianCalendar().apply {
            add(Calendar.HOUR, 6)
        }
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(gregorianCalendar.time)

        changingDeviceToken = AeSimpleSHA1.SHA1(androidId + dateString) ?: ""
    }
}
