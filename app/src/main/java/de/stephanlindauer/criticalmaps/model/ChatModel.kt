package de.stephanlindauer.criticalmaps.model

import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatModel @Inject constructor(
    private val userModel: UserModel
) {
    private var receivedChatMessages = mutableListOf<ReceivedChatMessage>()

    fun getReceivedChatMessages(): List<ReceivedChatMessage> = receivedChatMessages

    @Throws(JSONException::class, UnsupportedEncodingException::class)
    fun setFromJson(jsonArray: JSONArray) {
        receivedChatMessages = ArrayList(jsonArray.length())

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val device = URLDecoder.decode(jsonObject.getString("device"), "UTF-8")
            val identifier = URLDecoder.decode(jsonObject.getString("identifier"), "UTF-8")
            val message = URLDecoder.decode(jsonObject.getString("message"), "UTF-8")
            val timestamp = Date(jsonObject.getString("timestamp").toLong() * 1000)

            receivedChatMessages.add(ReceivedChatMessage(message, timestamp))
        }

        receivedChatMessages.sortBy { it.timestamp }
    }

    fun createNewOutgoingMessage(message: String): JSONObject {
        val messageObject = JSONObject()
        try {
            messageObject.put("text", urlEncodeMessage(message))
            messageObject.put("identifier", AeSimpleSHA1.SHA1(message + Math.random()) ?: "")
            messageObject.put("device", userModel.changingDeviceToken)
        } catch (e: JSONException) {
            Timber.d(e)
        }
        return messageObject
    }

    private fun urlEncodeMessage(messageToEncode: String): String {
        return try {
            URLEncoder.encode(messageToEncode, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            ""
        }
    }

    companion object {
        const val MESSAGE_MAX_LENGTH = 255
    }
}
