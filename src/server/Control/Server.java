package server.Control;

import client.Entity.Message;
import client.Entity.User;
import server.Boundary.ServerView;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * The type Server.
 */
public class Server {
    private final int port;
    private Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>();
    private Queue<Message> undeliveredMessages = new ConcurrentLinkedQueue<>();
    private boolean running = true;
    private ServerView view;
    private List<User> allUsers = new ArrayList<>(); // Lista över alla registrerade användare

    /**
     * Instantiates a new Server.
     *
     * @param port the port
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Sets view.
     *
     * @param view the view
     */
    public void setView(ServerView view) {
        this.view = view;
    }

    /**
     * Start server.
     */
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

    /**
     * Stop server.
     */
    public void stopServer() {
        running = false;
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.closeConnection();
        }
        logMessage("Server stopped");
    }

    /**
     * Method for logging messages, which calls ServerView's logMessage method
     *
     * @param message the message
     */
    public void logMessage(String message) {
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.logMessage(message));  // Uppdatera GUI på EDT
        }
        System.out.println(message);
    }

    /**
     * Broadcast user update.
     */
    public synchronized void broadcastUserUpdate() {
        List<User> userList = new ArrayList<>(connectedUsers.values());
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendUserList(userList);
        }
    }

    /**
     * Add user.
     *
     * @param user the user
     */
    public synchronized void addUser(User user) {
        if (!allUsers.contains(user)) {
            allUsers.add(user);
        }
        connectedUsers.put(user.getName(), user);
        logMessage("User connected: " + user.getName());
        broadcastUserUpdate();

    }

    /**
     * Remove user.
     *
     * @param user the user
     */
    public synchronized void removeUser(User user) {
        connectedUsers.remove(user.getName());
        logMessage("User disconnected: " + user.getName());
        broadcastUserUpdate();
    }

    /**
     * Gets all users.
     *
     * @return the all users
     */
    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(allUsers);
    }

    /**
     * Send message.
     *
     * @param message the message
     */
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

    /**
     * Get clientHandler
     * @param user the user
     * @return ClientHandler
     */
    private ClientHandler getClientHandler(User user) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getUser().getName().equals(user.getName())) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Deliver undelivered messages.
     *
     * @param user the user
     */
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

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Server server = new Server(12345);

        SwingUtilities.invokeLater(() -> {
            ServerView serverView = new ServerView(server);
            server.setView(serverView);
            serverView.setVisible(true);
        });

    }

}
