package server;

import model.Message;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User user;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            user = (User) in.readObject();
            server.addUser(user);
            server.deliverUndeliveredMessages(user);

            Object obj;
            while ((obj = in.readObject()) != null) {
                if (obj instanceof Message) {
                    Message message = (Message) obj;
                    message.setDeliveredTime(null); // Reset delivered time for new delivery
                    server.sendMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            server.removeUser(user);
            closeConnection();
        }
    }

    public void sendUserList(List<User> userList) {
        try {
            out.writeObject(userList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            message.setDeliveredTime(java.time.LocalDateTime.now());
            out.writeObject(message);
            String senderUsername = message.getSender().getName();

            // Extract the receivers' usernames and join them into a single string
            String receiverUsernames = message.getReceivers().stream()
                    .map(User::getName)
                    .collect(Collectors.joining(", "));

            // Log the message
            ChatLogger.logMessage(senderUsername, receiverUsernames, message.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }

    void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
