package de.stephanlindauer.criticalmaps.model

import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1
import okhttp3.internal.Util
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatModel @Inject constructor(
    private val userModel: UserModel
) {

    private var receivedChatMessages = mutableListOf<ReceivedChatMessage>()

    fun getReceivedChatMessages(): List<ReceivedChatMessage> {
        return receivedChatMessages
    }

    @Throws(JSONException::class, UnsupportedEncodingException::class)
    fun setFromJson(jsonArray: JSONArray) {
        receivedChatMessages = mutableListOf()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val message = URLDecoder.decode(jsonObject.getString("message"), Util.UTF_8.name())
            val timestamp = Date(jsonObject.getString("timestamp").toLong() * 1000)

            receivedChatMessages.add(ReceivedChatMessage(message, timestamp))
        }

        receivedChatMessages.sortBy { it.timestamp }
    }

    fun createNewOutgoingMessage(message: String): JSONObject {
        val messageObject = JSONObject()
        try {
            messageObject.put("text", urlEncodeMessage(message))
            messageObject.put("identifier", AeSimpleSHA1.SHA1(message + Math.random()))
            messageObject.put("device", userModel.changingDeviceToken)
        } catch (e: JSONException) {
            Timber.d(e)
        }
        return messageObject
    }

    private fun urlEncodeMessage(messageToEncode: String): String {
        return try {
            URLEncoder.encode(messageToEncode, Util.UTF_8.name())
        } catch (e: UnsupportedEncodingException) {
            ""
        }
    }

    companion object {
        const val MESSAGE_MAX_LENGTH = 255
    }
}