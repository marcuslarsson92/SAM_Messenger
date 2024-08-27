package server.Control;

import client.Entity.Message;
import client.Entity.User;
import server.Boundary.ServerView;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private final int port;
    private Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>();
    private Queue<Message> undeliveredMessages = new ConcurrentLinkedQueue<>();
    private boolean running = true;
    private ServerView view;
    private List<User> allUsers = new ArrayList<>(); // Lista över alla registrerade användare

    public Server(int port) {
        this.port = port;
    }

    // Method to set the ServerView, so the Server can log messages
    public void setView(ServerView view) {
        this.view = view;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logMessage("Server started on port " + port);
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (running) {
                        logMessage("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logMessage("Server error: " + e.getMessage());
        }
    }

    public void stopServer() {
        running = false;
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.closeConnection();
        }
        logMessage("Server stopped");
    }

    // Method for logging messages, which calls ServerView's logMessage method
    public void logMessage(String message) {
        /*
        if (view != null) {
            view.logMessage(message);
        }

         */
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.logMessage(message));  // Uppdatera GUI på EDT
        }
        System.out.println(message); // Fallback if no view is set
    }

    public synchronized void broadcastUserUpdate() {
        List<User> userList = new ArrayList<>(connectedUsers.values());
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendUserList(userList);
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
    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(allUsers);
    }

    public synchronized void sendMessage(Message message) {
        boolean delivered = false;
        for (User receiver : message.getReceivers()) {
            ClientHandler handler = getClientHandler(receiver);
            if (handler != null) {
                handler.sendMessage(message);
                delivered = true;
            }
        }
        if (!delivered) {
            undeliveredMessages.add(message);
            logMessage("Message from " + message.getSender().getName() + " to " +
                    message.getReceivers().toString() + " queued");
        } else {
            logMessage("Message from " + message.getSender().getName() + " to " +
                    message.getReceivers().toString() + " delivered");
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
                    logMessage("Queued message delivered to " + user.getName());
                }
            }
        }
    }
    public static void main(String[] args) {
        Server server = new Server(12345);

        SwingUtilities.invokeLater(() -> {
            ServerView serverView = new ServerView(server);
            server.setView(serverView);
            serverView.setVisible(true);
        });

    }

}
