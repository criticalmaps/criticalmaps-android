package de.stephanlindauer.criticalmaps.vo;

import com.crashlytics.android.Crashlytics;
import de.stephanlindauer.criticalmaps.helper.AeSimpleSHA1;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

public class OutgoingChatMessage {

    private final Date timestamp;
    private final String urlEncodedMessage;
    private final String identifier;

    public OutgoingChatMessage(String message) {
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
