package de.stephanlindauer.criticalmass.model;

import de.stephanlindauer.criticalmass.vo.ChatMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class ChatModel {

    private ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

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
            chatMessages = new ArrayList<ChatMessage>();
        } else {
            chatMessages.clear();
        }

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject value = jsonObject.getJSONObject(key);
            String message = URLDecoder.decode(value.getString("message"), "UTF-8");
            Date timestamp = new Date(Long.parseLong(value.getString("timestamp")) * 1000);

            chatMessages.add(new ChatMessage(message, timestamp));
        }

        Collections.sort(chatMessages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage oneChatMessages, ChatMessage otherChatMessage) {
                return oneChatMessages.getTimestamp().compareTo(otherChatMessage.getTimestamp());
            }
        });
    }

    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }


}
