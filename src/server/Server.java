package server;

import model.Message;
import model.User;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    private final int port;
    private Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>();
    private Queue<Message> undeliveredMessages = new ConcurrentLinkedQueue<>();
    private boolean running = true;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server(int port) {
        this.port = port;
        setupLogger();
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

    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("server_log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
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

    public synchronized void broadcastUserUpdate() {
        List<User> userList = new ArrayList<>(connectedUsers.values());
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendUserList(userList);
        }
    }

    public synchronized void addUser(User user) {
        connectedUsers.put(user.getName(), user);
        logMessage("User connected: " + user.getName());
        broadcastUserUpdate();
    }

    public synchronized void removeUser(User user) {
        connectedUsers.remove(user.getName());
        logMessage("User disconnected: " + user.getName());
        broadcastUserUpdate();
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

    public void logMessage(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        Server server = new Server(12345);
        server.start();
    }
}
