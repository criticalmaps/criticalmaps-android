package de.stephanlindauer.criticalmaps.model.chat;

import java.util.Date;


public class ReceivedChatMessage {

    private final Date timestamp;
    private final String message;

    public ReceivedChatMessage(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
