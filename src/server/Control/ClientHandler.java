package server.Control;

import client.Entity.Message;
import client.Entity.User;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * The type Client handler.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User user;

    /**
     * Instantiates a new Client handler.
     *
     * @param socket the socket
     * @param server the server
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }
    /**
     * Handles the client communication in a separate thread.
     * This method initializes input and output streams, receives the user object, and processes incoming messages.
     * Messages are sent to the server for delivery, and undelivered messages are handled when the user connects.
     */
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
        }
    }

    /**
     * Send user list.
     *
     * @param userList the user list
     */
    public void sendUserList(List<User> userList) {
        try {
            out.writeObject(userList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send message.
     *
     * @param message the message
     */
    public void sendMessage(Message message) {
        try {
            message.setDeliveredTime(java.time.LocalDateTime.now());
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Close connection.
     */
    void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
