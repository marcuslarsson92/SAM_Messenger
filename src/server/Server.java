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
    private List<User> allUsers = new ArrayList<>(); // Lista över alla registrerade användare

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

    // Uppdaterad metod för att skicka meddelanden, hanterar nu gruppmeddelanden
    public synchronized void sendMessage(Message message) {
        boolean delivered = false;
        for (User receiver : message.getReceivers()) { // Loopa genom alla mottagare av meddelandet
            ClientHandler handler = getClientHandler(receiver); // Hämta ClientHandler för varje mottagare
            if (handler != null) {
                handler.sendMessage(message); // Skicka meddelandet till mottagaren
                delivered = true; // Indikerar att meddelandet levererats
            }
        }
        if (!delivered) { // Om inget meddelande kunde levereras
            undeliveredMessages.add(message); // Lägg till meddelandet i kön för odelade meddelanden
            logMessage("Message from " + message.getSender().getName() + " to " +
                    message.getReceivers().toString() + " queued"); // Logga att meddelandet köades
        } else {
            logMessage("Message from " + message.getSender().getName() + " to " +
                    message.getReceivers().toString() + " delivered"); // Logga att meddelandet levererades
        }
    }

    // Metod för att hämta ClientHandler för en specifik användare
    private ClientHandler getClientHandler(User user) {
        for (ClientHandler handler : clientHandlers) { // Iterera genom alla ClientHandlers
            if (handler.getUser().getName().equals(user.getName())) { // Kontrollera om användarnamnet matchar
                return handler; // Returnera rätt ClientHandler
            }
        }
        return null; // Returnera null om ingen matchande ClientHandler hittas
    }

    public void deliverUndeliveredMessages(User user) {
        Iterator<Message> iterator = undeliveredMessages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message.getReceivers().contains(user)) { // Kontrollera om mottagaren finns i listan över mottagare
                ClientHandler handler = getClientHandler(user);
                if (handler != null) {
                    handler.sendMessage(message);
                    iterator.remove(); // Ta bort meddelandet från kön efter att det levererats
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
