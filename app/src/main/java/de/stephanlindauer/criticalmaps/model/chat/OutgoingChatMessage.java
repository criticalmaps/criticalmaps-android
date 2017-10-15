package de.stephanlindauer.criticalmaps.model.chat;

import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import okhttp3.internal.Util;

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

    @Override
    public String getMessage() {
        return message;
    }

    public String getIdentifier() {
        return identifier;
    }

    private String urlEncodeMessage(String messageToEncode) {
        try {
            return URLEncoder.encode(messageToEncode, Util.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
