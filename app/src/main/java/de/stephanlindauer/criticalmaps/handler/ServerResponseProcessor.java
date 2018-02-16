package de.stephanlindauer.criticalmaps.handler;

import org.json.JSONObject;

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

    public void process(final String jsonString) {
        try {
            final JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("locations")) {
                otherUsersLocationModel.setFromJson(jsonObject.getJSONObject("locations"));
            }
            if (jsonObject.has("locations")) {
                chatModel.setFromJson(jsonObject.getJSONObject("chatMessages"));
            }
            eventBus.post(Events.NEW_SERVER_RESPONSE_EVENT);
        } catch (Exception e) {
            Timber.d(e);
        }
    }
}
