package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private final User sender;
    private final User recipient;
    private final String content;
    private final LocalDateTime timestamp;

    public Message(User sender, User recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

