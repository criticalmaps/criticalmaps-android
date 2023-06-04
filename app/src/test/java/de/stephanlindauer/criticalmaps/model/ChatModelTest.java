package de.stephanlindauer.criticalmaps.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage;

import static com.google.common.truth.Truth.assertThat;

public class ChatModelTest {
    @Test
    public void hasOutgoingChatmessages_isTrueIfOutgoingMessagesExist() {
        final ChatModel tested = new ChatModel();
        tested.setNewOutgoingMessage(new OutgoingChatMessage("test"));

        assertThat(tested.hasOutgoingMessages()).isTrue();
    }

    @Test
    public void hasOutgoingChatmessages_isFalseIfNoOutgoingMessagesExist() {
        final ChatModel tested = new ChatModel();

        assertThat(tested.hasOutgoingMessages()).isFalse();
    }

    @Test
    public void setFromJson_testThatChatmessagesAreSorted() throws IOException, URISyntaxException,
            JSONException {
        final String json = readToString(new File(getClass().getClassLoader()
                .getResource("simple_server_response.json").toURI()));
        final JSONObject response = new JSONObject(json);
        final ChatModel tested = new ChatModel();

        tested.setFromJson(response.getJSONObject("chatMessages"));
        final ReceivedChatMessage message0 =
                (ReceivedChatMessage) tested.getSavedAndOutgoingMessages().get(0);
        final ReceivedChatMessage message1 =
                (ReceivedChatMessage) tested.getSavedAndOutgoingMessages().get(1);

        assertThat(message0.getTimestamp()).isLessThan(message1.getTimestamp());
    }

    @Test
    public void setFromJson_outgoingMessagesAreRemovedAfterSend() throws JSONException,
            UnsupportedEncodingException {
        final OutgoingChatMessage outgoingChatMessage = new OutgoingChatMessage("test");
        final JSONObject testResponse = new JSONObject("{\"" + outgoingChatMessage.getIdentifier()
                + "\":{\"message\":\"" + outgoingChatMessage.getMessage()
                + "\",\"timestamp\":1446113099}}");
        final ChatModel tested = new ChatModel();

        tested.setFromJson(testResponse);
        tested.setNewOutgoingMessage(outgoingChatMessage);
        tested.setFromJson(testResponse);

        assertThat(tested.hasOutgoingMessages()).isFalse();
    }

    @Test
    public void setFromJson_existingMessagesAreReplaced() throws URISyntaxException, IOException,
            JSONException {
        final String json = readToString(new File(getClass().getClassLoader()
                .getResource("simple_server_response.json").toURI()));
        final JSONObject testResponse = new JSONObject(json).getJSONObject("chatMessages");
        final ChatModel tested = new ChatModel();

        tested.setFromJson(testResponse);
        final int sizeBefore = tested.getSavedAndOutgoingMessages().size();

        tested.setFromJson(testResponse);
        assertThat(tested.getSavedAndOutgoingMessages()).hasSize(sizeBefore);
    }

    public String readToString(File file) throws IOException {
        return readToString(Charset.defaultCharset(), file);
    }

    public String readToString(Charset charset, File file) throws IOException {
        final FileInputStream stream = new FileInputStream(file);
        try {
            return readToStringFromFileInputStream(charset, stream);
        } finally {
            stream.close();
        }
    }

    private String readToStringFromFileInputStream(Charset charset, FileInputStream stream) throws IOException {
        final FileChannel fc = stream.getChannel();
        try {
            final MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return charset.decode(bb).toString();
        } finally {
            fc.close();
        }
    }
}
