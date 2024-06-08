package model;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Buffer {
    private ConcurrentLinkedQueue<Message> messageQueue;

    public Buffer() {
        messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void addMessage(Message message) {
        messageQueue.add(message);
    }

    public Message getMessage() {
        return messageQueue.poll();
    }

    public boolean isEmpty() {
        return messageQueue.isEmpty();
    }
}
