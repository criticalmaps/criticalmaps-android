package de.stephanlindauer.criticalmaps.handler;

import org.json.JSONArray;

import javax.inject.Inject;

import dagger.Reusable;
import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.provider.EventBus;
import timber.log.Timber;

@Reusable
public class ServerResponseProcessor {

    private final OtherUsersLocationModel otherUsersLocationModel;
    private final EventBus eventBus;
    private final ChatModel chatModel;

    @Inject
    public ServerResponseProcessor(OtherUsersLocationModel otherUsersLocationModel, EventBus eventBus, ChatModel chatModel) {
        this.otherUsersLocationModel = otherUsersLocationModel;
        this.eventBus = eventBus;
        this.chatModel = chatModel;
    }

    public void processLocations(final String jsonString) {
        try {
            final JSONArray jsonArray = new JSONArray(jsonString);
            otherUsersLocationModel.setFromJson(jsonArray);
            eventBus.post(Events.NEW_SERVER_RESPONSE_EVENT);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void processChatmessages(final String jsonString) {
        try {
            final JSONArray jsonArray = new JSONArray(jsonString);
            chatModel.setFromJson(jsonArray);
            eventBus.post(Events.NEW_SERVER_RESPONSE_EVENT);
        } catch (Exception e) {
            Timber.d(e);
        }
    }
}
