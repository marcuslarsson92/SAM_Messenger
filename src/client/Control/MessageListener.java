package client.Control;

import client.Entity.Message;

public interface MessageListener {
    void onMessageReceived(Message message);
}
