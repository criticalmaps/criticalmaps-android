package de.stephanlindauer.criticalmaps.model;

import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;
import de.stephanlindauer.criticalmaps.model.chat.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import okhttp3.internal.Util;
import timber.log.Timber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatModel {

    private final List<OutgoingChatMessage> outgoingMessages = new ArrayList<>();
    private List<ReceivedChatMessage> chatMessages = new ArrayList<>();

    @Inject
    public ChatModel() {
    }

    public void setFromJson(JSONObject jsonObject) throws JSONException,
            UnsupportedEncodingException {
        chatMessages = new ArrayList<>(jsonObject.length());

        Iterator<String> identifiers = jsonObject.keys();
        while (identifiers.hasNext()) {
            String identifier = identifiers.next();
            JSONObject value = jsonObject.getJSONObject(identifier);
            String message = URLDecoder.decode(value.getString("message"), Util.UTF_8.name());
            Date timestamp = new Date(Long.parseLong(value.getString("timestamp")) * 1000);

            Iterator<OutgoingChatMessage> outgoingChatMessageIterator = outgoingMessages.iterator();
            while (outgoingChatMessageIterator.hasNext()) {
                if (outgoingChatMessageIterator.next().getIdentifier().equals(identifier)) {
                    outgoingChatMessageIterator.remove();
                }
            }

            chatMessages.add(new ReceivedChatMessage(message, timestamp));
        }

        Collections.sort(chatMessages, new Comparator<ReceivedChatMessage>() {
            @Override
            public int compare(ReceivedChatMessage oneChatMessages,
                               ReceivedChatMessage otherChatMessage) {
                return oneChatMessages.getTimestamp().compareTo(otherChatMessage.getTimestamp());
            }
        });
    }

    public void setNewOutgoingMessage(OutgoingChatMessage newOutgoingMessage) {
        outgoingMessages.add(newOutgoingMessage);
    }

    public JSONArray getOutgoingMessagesAsJson() {
        JSONArray jsonArray = new JSONArray();

        for (OutgoingChatMessage outgoingChatMessage : outgoingMessages) {
            try {
                JSONObject messageObject = new JSONObject();
                messageObject.put("text", outgoingChatMessage.getUrlEncodedMessage());
                messageObject.put("timestamp", outgoingChatMessage.getTimestamp().getTime());
                messageObject.put("identifier", outgoingChatMessage.getIdentifier());
                jsonArray.put(messageObject);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
        return jsonArray;
    }

    public ArrayList<IChatMessage> getSavedAndOutgoingMessages() {
        int mergedListsSize = chatMessages.size() + outgoingMessages.size();
        ArrayList<IChatMessage> mergeArrayList = new ArrayList<>(mergedListsSize);
        mergeArrayList.addAll(chatMessages);
        mergeArrayList.addAll(outgoingMessages);
        return mergeArrayList;
    }

    public boolean hasOutgoingMessages() {
        return !outgoingMessages.isEmpty();
    }
}
