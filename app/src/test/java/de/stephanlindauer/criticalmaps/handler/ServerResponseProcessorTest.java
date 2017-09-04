package de.stephanlindauer.criticalmaps.handler;

import org.junit.Test;
import org.ligi.axt.AXT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.provider.EventBus;
import de.stephanlindauer.criticalmaps.vo.chat.ReceivedChatMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ServerResponseProcessorTest {

    @Test
    public void testThatBasicChatMessagesAreParsed() throws IOException, URISyntaxException {
        final String json = AXT.at(new File(getClass().getClassLoader().getResource("simple_server_response.json").toURI())).readToString();
        final ChatModel chatModel = new ChatModel();
        final ServerResponseProcessor tested = new ServerResponseProcessor(mock(OtherUsersLocationModel.class), mock(EventBus.class), chatModel);

        tested.process(json);

        assertThat(chatModel.getSavedAndOutgoingMessages().size()).isEqualTo(2);
    }

    @Test
    public void testThatBasicChatMessagesAreSorted() throws IOException, URISyntaxException {
        final String json = AXT.at(new File(getClass().getClassLoader().getResource("simple_server_response.json").toURI())).readToString();
        final ChatModel chatModel = new ChatModel();
        final ServerResponseProcessor tested = new ServerResponseProcessor(mock(OtherUsersLocationModel.class), mock(EventBus.class), chatModel);

        tested.process(json);

        final ReceivedChatMessage message0 = (ReceivedChatMessage) chatModel.getSavedAndOutgoingMessages().get(0);
        final ReceivedChatMessage message1 = (ReceivedChatMessage) chatModel.getSavedAndOutgoingMessages().get(1);

        assertThat(message0.getTimestamp()).isBefore(message1.getTimestamp());
    }


    @Test
    public void testThatEventIsFiredForValidJSON() throws IOException, URISyntaxException {
        final String json = AXT.at(new File(getClass().getClassLoader().getResource("simple_server_response.json").toURI())).readToString();
        final EventBus eventMock = mock(EventBus.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(mock(OtherUsersLocationModel.class), eventMock, mock(ChatModel.class));

        tested.process(json);

        verify(eventMock, times(1)).post(Events.NEW_SERVER_RESPONSE_EVENT);
    }

    @Test
    public void testThatNoEventIsFiredForInvalidJSON() throws IOException, URISyntaxException {
        final EventBus eventMock = mock(EventBus.class);
        final ServerResponseProcessor tested = new ServerResponseProcessor(mock(OtherUsersLocationModel.class), eventMock, mock(ChatModel.class));

        tested.process("borken");

        verify(eventMock,never()).post(Events.NEW_SERVER_RESPONSE_EVENT);
    }
}
