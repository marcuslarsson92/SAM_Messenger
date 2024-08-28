package client.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.Icon;

/**
 * The type Message.
 */
public class Message implements Serializable {
    private User sender;
    private User receiver;
    private List<User> receivers;

    private String text;
    private Icon image;
    private LocalDateTime receivedTime;
    private LocalDateTime deliveredTime;

    /**
     * Instantiates a new Message.
     *
     * @param sender    the sender
     * @param receivers the receivers
     * @param text      the text
     * @param image     the image
     */
    public Message(User sender, List<User> receivers, String text, Icon image) {
        this.sender = sender;
        this.receivers = receivers;
        this.text = text;
        this.image = image;
        this.receivedTime = LocalDateTime.now();
    }

    /**
     * Gets sender.
     *
     * @return the sender
     */
    public User getSender() {
        return sender;
    }

    /**
     * Gets receivers.
     *
     * @return the receivers
     */
    public List<User> getReceivers() {
        return receivers;
    }

    /**
     * To String method.
     * @return String of recievers
     */
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


    /**
     * Gets text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets image.
     *
     * @return the image
     */
    public Icon getImage() {
        return image;
    }

    /**
     * Gets received time.
     *
     * @return the received time
     */
    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    /**
     * Gets delivered time.
     *
     * @return the delivered time
     */
    public LocalDateTime getDeliveredTime() {
        return deliveredTime;
    }

    /**
     * Sets delivered time.
     *
     * @param deliveredTime the delivered time
     */
    public void setDeliveredTime(LocalDateTime deliveredTime) {
        this.deliveredTime = deliveredTime;
    }
}
