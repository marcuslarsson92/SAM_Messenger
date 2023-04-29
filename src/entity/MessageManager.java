package entity;

import java.util.ArrayList;

public class MessageManager extends Thread {
    private Buffer<Message> messageBuffer;
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public MessageManager (Buffer<Message> messageBuffer){
        this.messageBuffer = messageBuffer;
    }
    public void addListener(MessageListener listener){
        messageListeners.add(listener);
    }
    @Override
    public void start() {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        connectionHandler.start();
    }

    private class ConnectionHandler extends Thread{
        public void run() {
            while(!interrupted()){
                try {
                    Message message = messageBuffer.get();
                    for(MessageListener listener: messageListeners){
                        listener.message(message);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}