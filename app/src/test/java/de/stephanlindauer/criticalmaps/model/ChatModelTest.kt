package de.stephanlindauer.criticalmaps.model

import com.google.common.truth.Truth.assertThat
import org.json.JSONArray
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File

class ChatModelTest {

    @Test
    fun `setFromJson - test that chatmessages are sorted`() {
        val json = readToString(File(javaClass.classLoader!!
            .getResource("server_response_chatmessages.json")!!.toURI()))
        val response = JSONArray(json)

        val userModel = mock(UserModel::class.java)
        `when`(userModel.changingDeviceToken).thenReturn("t0k3n")

        val tested = ChatModel(userModel)

        tested.setFromJson(response)
        val message0 = tested.getReceivedChatMessages()[0]
        val message1 = tested.getReceivedChatMessages()[1]

        assertThat(message0.timestamp).isLessThan(message1.timestamp)
    }

    @Test
    fun `setFromJson - existing messages are replaced`() {
        val json = readToString(File(javaClass.classLoader!!
            .getResource("server_response_chatmessages.json")!!.toURI()))
        val testResponse = JSONArray(json)

        val userModel = mock(UserModel::class.java)
        `when`(userModel.changingDeviceToken).thenReturn("t0k3n")

        val tested = ChatModel(userModel)

        tested.setFromJson(testResponse)
        val sizeBefore = tested.getReceivedChatMessages().size

        tested.setFromJson(testResponse)
        assertThat(tested.getReceivedChatMessages()).hasSize(sizeBefore)
    }

    private fun readToString(file: File) = file.readText()
}
