package client.Control;

import client.Entity.Message;

/**
 * The interface Message listener.
 */
public interface MessageListener {
    /**
     * On message received.
     *
     * @param message the message
     */
    void onMessageReceived(Message message);
}
