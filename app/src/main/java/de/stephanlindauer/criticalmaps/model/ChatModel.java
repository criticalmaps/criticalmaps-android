package de.stephanlindauer.criticalmaps.model;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.stephanlindauer.criticalmaps.model.chat.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage;
import okhttp3.internal.Util;
import timber.log.Timber;

@Singleton
public class ChatModel {
    public static int MESSAGE_MAX_LENGTH = 255;

    private final UserModel userModel;

    private List<ReceivedChatMessage> receivedChatMessages = new ArrayList<>();

    @Inject
    public ChatModel(UserModel userModel) {
        this.userModel = userModel;
    }

    @NonNull
    public List<ReceivedChatMessage> getReceivedChatMessages() {
        return this.receivedChatMessages;
    }

    public void setFromJson(JSONArray jsonArray) throws JSONException,
            UnsupportedEncodingException {
        receivedChatMessages = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String device = URLDecoder.decode(jsonObject.getString("device"), Util.UTF_8.name());
            String identifier = URLDecoder.decode(jsonObject.getString("identifier"), Util.UTF_8.name());
            String message = URLDecoder.decode(jsonObject.getString("message"), Util.UTF_8.name());
            Date timestamp = new Date(Long.parseLong(jsonObject.getString("timestamp")) * 1000);

            receivedChatMessages.add(new ReceivedChatMessage(message, timestamp));
        }

        Collections.sort(receivedChatMessages, new Comparator<ReceivedChatMessage>() {
            @Override
            public int compare(ReceivedChatMessage oneChatMessages,
                               ReceivedChatMessage otherChatMessage) {
                return oneChatMessages.getTimestamp().compareTo(otherChatMessage.getTimestamp());
            }
        });
    }

    public void sendNewOutgoingMessage(OutgoingChatMessage newOutgoingMessage) {
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put("text", newOutgoingMessage.getUrlEncodedMessage());
            messageObject.put("timestamp", newOutgoingMessage.getTimestamp().getTime());
            messageObject.put("identifier", newOutgoingMessage.getIdentifier());
            messageObject.put("device", userModel.getChangingDeviceToken());
        } catch (JSONException e) {
            Timber.d(e);
        }
    }
}
