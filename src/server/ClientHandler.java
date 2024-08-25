package server;

import model.Message;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User user;
    private FileController fileController;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        fileController = new FileController(user.getName());
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
            //lägg in i fil
            List <User> rec = message.getReceivers();
            if (rec.size() == 1) {
                User user = (User)rec.get(1);
                user.getName(); //
            }
            message.getReceivers();

            //FileHandler senderFileHandler = new FileHandler(client.getUser().getName());
            //senderFileHandler.logMessageSent(client.getUser().getName(), String.valueOf(receiver), message.getText());

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
