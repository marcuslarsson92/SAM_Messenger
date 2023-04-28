import model.User;
import control.Controller;
import control.*;

import java.util.Date;

public class Message {
    private String text;
    private User sender;
    private User reciever;
    private Date sent;
    private Date delivered;

    public Message(String text, User sender, User reciever, Date sent, Date delivered) {
        this.text = text;
        this.sender = sender;
        this.reciever = reciever;
        this.sent = sent;
        this.delivered = delivered;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReciever() {
        return reciever;
    }

    public void setReciever(User reciever) {
        this.reciever = reciever;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public Date getDelivered() {
        return delivered;
    }

    public void setDelivered(Date delivered) {
        this.delivered = delivered;
    }
}