package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Icon;

public class Message implements Serializable {
    private User sender;
    private User receiver;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < receivers.size(); i++) {
            sb.append(receivers.get(i).toString());
            if (i < receivers.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
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
