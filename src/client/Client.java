package client;

import model.Message;
import model.MessageListener;
import model.User;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client {
    private User user;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessageListener messageListener;
    private UserListListener userListListener;

    public Client(User user, String serverAddress, int serverPort) throws IOException {
        this.user = user;
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        // Send user information to server
        out.writeObject(user);

        // Start listening for messages
        new Thread(new Listener()).start();
    }

    public User getUser() {
        return user;
    }

    public void sendMessage(Message message) throws IOException {
        System.out.println("Inne i " + user.getName() + "client i sendMessage metoden. Sender = " + message.getSender().getName() + " och receiver = " + message.getReceivers().get(0).getName());
        out.writeObject(message);

    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setUserListListener(UserListListener userListListener) {
        this.userListListener = userListListener;
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                Object obj;
                while ((obj = in.readObject()) != null) {
                    if (obj instanceof Message) {
                        if (messageListener != null) {
                            System.out.println("Inne i run metoden och tar emot Message-objekt i Client: " + user.getName());
                            messageListener.onMessageReceived((Message) obj);
                        }
                    } else if (obj instanceof List) {
                        if (userListListener != null) {
                            userListListener.onUserListUpdated((List<User>) obj);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
