package de.stephanlindauer.criticalmaps.vo.chat;

import de.stephanlindauer.criticalmaps.helper.AeSimpleSHA1;

import java.util.Date;

public class ReceivedChatMessage implements IChatMessage {

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
