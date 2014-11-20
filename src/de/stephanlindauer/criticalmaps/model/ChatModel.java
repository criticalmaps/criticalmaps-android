package de.stephanlindauer.criticalmaps.model;

import com.crashlytics.android.Crashlytics;
import de.stephanlindauer.criticalmaps.vo.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.vo.ReceivedChatMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class ChatModel {

    private ArrayList<ReceivedChatMessage> chatMessages = new ArrayList<ReceivedChatMessage>();
    private ArrayList<OutgoingChatMessage> outgoingMassages = new ArrayList<>();

    //singleton
    private static ChatModel instance;

    public static ChatModel getInstance() {
        if (ChatModel.instance == null) {
            ChatModel.instance = new ChatModel();
        }
        return ChatModel.instance;
    }

    public void setNewJson(JSONObject jsonObject) throws JSONException, UnsupportedEncodingException {
        if (chatMessages == null) {
            chatMessages = new ArrayList<ReceivedChatMessage>();
        } else {
            chatMessages.clear();
        }

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject value = jsonObject.getJSONObject(key);
            String message = URLDecoder.decode(value.getString("message"), "UTF-8");
            Date timestamp = new Date(Long.parseLong(value.getString("timestamp")) * 1000);
            String identifier = key;
            //TODO remove via identifyer

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

    public JSONObject getOutgoingMessagesAsJson() {
        JSONObject jsonObject = new JSONObject();
        for (OutgoingChatMessage outgoingChatMessage : outgoingMassages) {
            try {
                jsonObject.put(outgoingChatMessage.getIdentifier(), outgoingChatMessage.getUrlEncodedMessage() );
            } catch (JSONException e) {
                Crashlytics.logException(e);
            }
        }
        return jsonObject;
    }

    public ArrayList<ReceivedChatMessage> getChatMessages() {
        return chatMessages;
    }

    public boolean hasOutgoingMessages() {
        return outgoingMassages.size() > 0;
    }
}
