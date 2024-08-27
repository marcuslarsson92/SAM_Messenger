package server;

import model.Message;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User user;
    private FileController fileController;
    private Map<User, FileController> receiverFCs = new HashMap<>();  // För att hålla mottagarnas FileController

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
            fileController = new FileController(user);
            System.out.println("FileController created for: " + user.getName());

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

    public void sendMessage(Message msg) {
        try {
            msg.setDeliveredTime(java.time.LocalDateTime.now());
            out.writeObject(msg);

            System.out.println("FileController created for: " + user.getName());

            fileController.logMessageSent(msg.getSender().getName(), getReceiverNames(msg), msg.getText());
            System.out.println("FÖRSTA LOGMESSAGESENT, msg.getSender() = " + msg.getSender().getName());

            for (User receiver : msg.getReceivers()) {
                FileController receiverFC = receiverFCs.computeIfAbsent(receiver, n -> new FileController(receiver));
                System.out.println("Receiver.getname = " + receiver.getName());
                receiverFC.logMessageSent(msg.getSender().getName(), receiver.getName(), msg.getText());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getReceiverNames(Message message) {
        StringBuilder names = new StringBuilder();
        for (User user : message.getReceivers()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(user.getName());
        }
        return names.toString();
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
