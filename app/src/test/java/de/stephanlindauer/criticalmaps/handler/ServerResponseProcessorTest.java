package de.stephanlindauer.criticalmaps.handler;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.provider.EventBus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ServerResponseProcessorTest {
    @Test
    public void process_chatmessagesAreSetOnModel() throws IOException, URISyntaxException,
            JSONException {
        final String json = readToString(new File(getClass().getClassLoader()
                .getResource("simple_server_response.json").toURI()));
        final ChatModel chatModel = mock(ChatModel.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(
                mock(OtherUsersLocationModel.class), mock(EventBus.class), chatModel);

        tested.processLocations(json);

        verify(chatModel).setFromJson(any(JSONObject.class));
    }

    @Test
    public void process_eventIsFiredForValidJSON() throws IOException, URISyntaxException {
        final String json = readToString(new File(getClass().getClassLoader()
                .getResource("simple_server_response.json").toURI()));
        final EventBus eventMock = mock(EventBus.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(
                mock(OtherUsersLocationModel.class), eventMock, mock(ChatModel.class));

        tested.processLocations(json);

        verify(eventMock, times(1)).post(Events.NEW_SERVER_RESPONSE_EVENT);
    }

    @Test
    public void process_noEventIsFiredForInvalidJSON() {
        final EventBus eventMock = mock(EventBus.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(
                mock(OtherUsersLocationModel.class), eventMock, mock(ChatModel.class));

        tested.processLocations("borken");

        verify(eventMock, never()).post(Events.NEW_SERVER_RESPONSE_EVENT);
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
