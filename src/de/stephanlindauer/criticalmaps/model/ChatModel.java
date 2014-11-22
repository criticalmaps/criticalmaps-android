package de.stephanlindauer.criticalmaps.model;

import com.crashlytics.android.Crashlytics;
import de.stephanlindauer.criticalmaps.vo.chat.IChatMessage;
import de.stephanlindauer.criticalmaps.vo.chat.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.vo.chat.ReceivedChatMessage;
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

            for (int i = outgoingMassages.size() - 1; i > -1; i--) {
                OutgoingChatMessage outgoingMessageToMaybeDelete = outgoingMassages.get(i);
                if (outgoingMessageToMaybeDelete.getIdentifier().equals(identifier)) {
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

    public JSONObject getOutgoingMessagesAsJson(String uniqueDeviceIdHashed) {
        JSONObject jsonObject = new JSONObject();
        Integer counter = 0;

        for (int i = 0; i < outgoingMassages.size(); i++) {
            OutgoingChatMessage outgoingChatMessage = outgoingMassages.get(i);
            try {
                JSONObject messageObject = new JSONObject();
                messageObject.put("text", outgoingChatMessage.getUrlEncodedMessage());
                messageObject.put("timestamp", outgoingChatMessage.getTimestamp().getTime());
                messageObject.put("uniqueDeviceIdHashed", uniqueDeviceIdHashed);
                messageObject.put("identifier", outgoingChatMessage.getIdentifier());
                jsonObject.put(counter.toString(), messageObject);
                counter++;
            } catch (JSONException e) {
                Crashlytics.logException(e);
            }
        }
        return jsonObject;
    }

    public ArrayList<IChatMessage> getSavedAndOutgoingMessages() {
        ArrayList<IChatMessage> mergeArrayList = new ArrayList<IChatMessage>();
        mergeArrayList.addAll(chatMessages);
        mergeArrayList.addAll(outgoingMassages);
        return mergeArrayList;
    }

    public boolean hasOutgoingMessages() {
        return outgoingMassages.size() > 0;
    }
}
