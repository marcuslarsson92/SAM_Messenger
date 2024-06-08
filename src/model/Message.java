package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.Icon;

public class Message implements Serializable {
    private User sender;
    private List<User> receivers;
    private String text;
    private Icon image;
    private LocalDateTime receivedTime;
    private LocalDateTime deliveredTime;

    public Message(User sender, List<User> receivers, String text, Icon image) {
        this.sender = sender;
        this.receivers = receivers;
        this.text = text;
        this.image = image;
        this.receivedTime = LocalDateTime.now();
    }

    public User getSender() {
        return sender;
    }

    public List<User> getReceivers() {
        return receivers;
    }

    public String getText() {
        return text;
    }

    public Icon getImage() {
        return image;
    }

    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    public LocalDateTime getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(LocalDateTime deliveredTime) {
        this.deliveredTime = deliveredTime;
    }
}
