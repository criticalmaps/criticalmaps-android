package de.stephanlindauer.criticalmaps.model;

import android.support.annotation.VisibleForTesting;

import com.squareup.okhttp.internal.Util;

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

import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;
import de.stephanlindauer.criticalmaps.vo.chat.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.vo.chat.ReceivedChatMessage;

public class ChatModel {

    private final ArrayList<OutgoingChatMessage> outgoingMassages = new ArrayList<>();

    private ArrayList<ReceivedChatMessage> chatMessages = new ArrayList<>();

    //singleton
    private static ChatModel instance;

    @VisibleForTesting
    public ChatModel() {}

    public static ChatModel getInstance() {
        if (ChatModel.instance == null) {
            ChatModel.instance = new ChatModel();
        }
        return ChatModel.instance;
    }

    public void setNewJson(JSONObject jsonObject) throws JSONException, UnsupportedEncodingException {
        if (chatMessages == null) {
            chatMessages = new ArrayList<>(jsonObject.length());
        } else {
            chatMessages.clear();
        }

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject value = jsonObject.getJSONObject(key);
            String message = URLDecoder.decode(value.getString("message"), Util.UTF_8.name());
            Date timestamp = new Date(Long.parseLong(value.getString("timestamp")) * 1000);

            //for i going backwards to delete without side-effects
            for (int i = outgoingMassages.size() - 1; i > -1; i--) {
                OutgoingChatMessage outgoingMessageToMaybeDelete = outgoingMassages.get(i);
                if (outgoingMessageToMaybeDelete.getIdentifier().equals(key)) {
                    outgoingMassages.remove(i);
                }
            }

            chatMessages.add(new ReceivedChatMessage(message, timestamp));
        }

        Collections.sort(chatMessages, new Comparator<ReceivedChatMessage>() {
            @Override
            public int compare(ReceivedChatMessage oneChatMessages, ReceivedChatMessage otherChatMessage) {
                return oneChatMessages.getTimestamp().compareTo(otherChatMessage.getTimestamp());
            }
        });
    }

    public void setNewOutgoingMessage(OutgoingChatMessage newOutgoingMessage) {
        outgoingMassages.add(newOutgoingMessage);
    }

    public JSONArray getOutgoingMessagesAsJson() {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0, size = outgoingMassages.size(); i < size; i++) {
            OutgoingChatMessage outgoingChatMessage = outgoingMassages.get(i);
            try {
                JSONObject messageObject = new JSONObject();
                messageObject.put("text", outgoingChatMessage.getUrlEncodedMessage());
                messageObject.put("timestamp", outgoingChatMessage.getTimestamp().getTime());
                messageObject.put("identifier", outgoingChatMessage.getIdentifier());
                jsonArray.put(messageObject);
            } catch (JSONException ignored) {
            }
        }
        return jsonArray;
    }

    public ArrayList<IChatMessage> getSavedAndOutgoingMessages() {
        int mergedListsSize = chatMessages.size() + outgoingMassages.size();
        ArrayList<IChatMessage> mergeArrayList = new ArrayList<>(mergedListsSize);
        mergeArrayList.addAll(chatMessages);
        mergeArrayList.addAll(outgoingMassages);
        return mergeArrayList;
    }

    public boolean hasOutgoingMessages() {
        return !outgoingMassages.isEmpty();
    }
}
