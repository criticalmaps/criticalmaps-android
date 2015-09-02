package de.stephanlindauer.criticalmaps.vo.chat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;
import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;

public class OutgoingChatMessage implements IChatMessage {

    private final Date timestamp;
    private final String urlEncodedMessage;
    private final String identifier;
    private final String message;

    public OutgoingChatMessage(String message) {
        this.message = message;
        this.urlEncodedMessage = urlEncodeMessage(message);
        this.timestamp = new Date();
        this.identifier = AeSimpleSHA1.SHA1(message + Math.random());
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUrlEncodedMessage() {
        return urlEncodedMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getIdentifier() {
        return identifier;
    }

    private String urlEncodeMessage(String messageToEncode) {
        try {
            return URLEncoder.encode(messageToEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }
}
