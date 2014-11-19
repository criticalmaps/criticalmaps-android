package de.stephanlindauer.criticalmaps.vo;

import java.util.Date;

public class ChatMessage {

    private final Date timestamp;
    private final String message;

    public ChatMessage(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
