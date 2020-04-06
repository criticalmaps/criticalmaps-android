package de.stephanlindauer.criticalmaps.handler;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.ligi.axt.AXT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
        final String json = AXT.at(new File(getClass().getClassLoader()
                .getResource("simple_server_response.json").toURI())).readToString();
        final ChatModel chatModel = mock(ChatModel.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(
                mock(OtherUsersLocationModel.class), mock(EventBus.class), chatModel);

        tested.process(json);

        verify(chatModel).setFromJson(any(JSONObject.class));
    }

    @Test
    public void process_eventIsFiredForValidJSON() throws IOException, URISyntaxException {
        final String json = AXT.at(new File(getClass().getClassLoader()
                .getResource("simple_server_response.json").toURI())).readToString();
        final EventBus eventMock = mock(EventBus.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(
                mock(OtherUsersLocationModel.class), eventMock, mock(ChatModel.class));

        tested.process(json);

        verify(eventMock, times(1)).post(Events.NEW_SERVER_RESPONSE_EVENT);
    }

    @Test
    public void process_noEventIsFiredForInvalidJSON() {
        final EventBus eventMock = mock(EventBus.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(
                mock(OtherUsersLocationModel.class), eventMock, mock(ChatModel.class));

        tested.process("borken");

        verify(eventMock, never()).post(Events.NEW_SERVER_RESPONSE_EVENT);
    }
}
