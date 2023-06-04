package de.stephanlindauer.criticalmaps.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatModelTest {

    @Test
    public void setFromJson_testThatChatmessagesAreSorted() throws IOException, URISyntaxException,
            JSONException {
        final String json = readToString(new File(getClass().getClassLoader()
                .getResource("server_response_chatmessages.json").toURI()));
        final JSONArray response = new JSONArray(json);

        UserModel userModel = mock(UserModel.class);
        when(userModel.getChangingDeviceToken()).thenReturn("t0k3n");

        final ChatModel tested = new ChatModel(userModel);

        tested.setFromJson(response);
        final ReceivedChatMessage message0 = tested.getReceivedChatMessages().get(0);
        final ReceivedChatMessage message1 = tested.getReceivedChatMessages().get(1);

        assertThat(message0.getTimestamp()).isLessThan(message1.getTimestamp());
    }


    @Test
    public void setFromJson_existingMessagesAreReplaced() throws URISyntaxException, IOException,
            JSONException {
        final String json = readToString(new File(getClass().getClassLoader()
                .getResource("server_response_chatmessages.json").toURI()));
        final JSONArray testResponse = new JSONArray(json);

        UserModel userModel = mock(UserModel.class);
        when(userModel.getChangingDeviceToken()).thenReturn("t0k3n");

        final ChatModel tested = new ChatModel(userModel);

        tested.setFromJson(testResponse);
        final int sizeBefore = tested.getReceivedChatMessages().size();

        tested.setFromJson(testResponse);
        assertThat(tested.getReceivedChatMessages()).hasSize(sizeBefore);
    }

    public String readToString(File file) throws IOException {
        return readToString(Charset.defaultCharset(), file);
    }

    public String readToString(Charset charset, File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return readToStringFromFileInputStream(charset, stream);
        }
    }

    private String readToStringFromFileInputStream(Charset charset, FileInputStream stream) throws IOException {
        try (FileChannel fc = stream.getChannel()) {
            final MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return charset.decode(bb).toString();
        }
    }
}
