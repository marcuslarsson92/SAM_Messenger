package server;

import model.Message;
import model.User;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private int port;
    private List<ClientHandler> clientHandlers;
    private List<Message> undeliveredMessages;
    private List<User> allUsers = new ArrayList<>(); // Lista över alla registrerade användare
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>();
    private boolean running = true;



    public Server(int port) {
        this.port = port;
        this.clientHandlers = new ArrayList<>();
        this.undeliveredMessages = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop() throws IOException {
        running = false;
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.closeConnection();
        }
        logMessage("Server stopped");
    }

    synchronized void sendMessage(Message message) {
        boolean delivered = false;

        // Skapa en FileController för avsändaren
        FileController senderFileController = new FileController(message.getSender());
        String senderName = message.getSender().getName();
        String receiverNames = message.getReceivers().stream().map(User::getName).collect(Collectors.joining(", "));
        senderFileController.logMessageSent(senderName, receiverNames, message.getText());

        for (User receiver : message.getReceivers()) {
            ClientHandler handler = getClientHandler(receiver);
            if (handler != null) {
                handler.sendMessage(message);
                delivered = true;

                // Skapa en FileController för mottagaren
                FileController receiverFileController = new FileController(receiver);
                receiverFileController.logMessageReceived(senderName, receiver.getName(), message.getText());
            }
        }

        if (!delivered) {
            undeliveredMessages.add(message);
            logMessage("Message from " + senderName + " to " + receiverNames + " queued");
        } else {
            logMessage("Message from " + senderName + " to " + receiverNames + " delivered");
        }
    }


    private ClientHandler getClientHandler(User user) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getUser().getName().equals(user.getName())) {
                return handler;
            }
        }
        return null;
    }

    public void deliverUndeliveredMessages(User user) {
        Iterator<Message> iterator = undeliveredMessages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message.getReceivers().contains(user)) {
                ClientHandler handler = getClientHandler(user);
                if (handler != null) {
                    handler.sendMessage(message);
                    iterator.remove();

                    // Logga att meddelandet levererades till användaren
                    FileController fileController = new FileController(user);
                    fileController.logMessageReceived(message.getSender().getName(),
                            user.getName(), message.getText());

                    logMessage("Queued message delivered to " + user.getName());
                }
            }
        }
    }
    public synchronized void addUser(User user) {
        if (!allUsers.contains(user)) {
            allUsers.add(user);
        }
        connectedUsers.put(user.getName(), user);
        logMessage("User connected: " + user.getName());
        broadcastUserUpdate();

    }

    public synchronized void removeUser(User user) {
        connectedUsers.remove(user.getName());
        logMessage("User disconnected: " + user.getName());
        broadcastUserUpdate();
    }
    public synchronized void broadcastUserUpdate() {
        List<User> userList = new ArrayList<>(connectedUsers.values());
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendUserList(userList);
        }
    }
    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(allUsers);
    }

    public void logMessage(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        Server server = new Server(12345);
        server.start();
    }
}

