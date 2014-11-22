package de.stephanlindauer.criticalmaps.vo.chat;

import com.crashlytics.android.Crashlytics;
import de.stephanlindauer.criticalmaps.helper.AeSimpleSHA1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

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
            Crashlytics.logException(e);
        }
        return "";
    }
}
