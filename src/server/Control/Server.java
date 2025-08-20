// src/server/Control/Server.java
package server.Control;

import client.Entity.Message;
import client.Entity.User;
import server.Boundary.ServerView;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private Map<String, List<Message>> offlineMessages = new ConcurrentHashMap<>(); // Map för att hålla offline-meddelanden
    private boolean running = true;
    private ServerView view;
    private List<User> allUsers = new ArrayList<>(); // Lista över alla registrerade användare

    public Server(int port, ServerView view) { // Modifierad konstruktor
        this.port = port;
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

    public void stop() {
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

        // Leverera sparade meddelanden om det finns några
        deliverUndeliveredMessages(user);

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

        // Skapa en FileController för avsändaren och servern
        FileController senderFileController = new FileController(message.getSender());
        FileController serverFileController = new FileController();

        senderFileController.logMessageSent(message.getSender().getName(), message.getReceiverNames(), message.getText());
        serverFileController.logMessageSent(message.getSender().getName(), message.getReceiverNames(), message.getText());

        for (User receiver : message.getReceivers()) {
            ClientHandler handler = getClientHandler(receiver);
            if (handler != null) {
                handler.sendMessage(message);
                delivered = true;

                // Skapa en FileController för mottagaren
                FileController receiverFileController = new FileController(receiver);
                receiverFileController.logMessageReceived(message.getSender().getName(), receiver.getName(), message.getText());
            } else {
                // Användaren är offline, spara meddelandet
                offlineMessages.computeIfAbsent(receiver.getName(), k -> new ArrayList<>()).add(message);
            }
        }

        if (!delivered) {
            logMessage("Message from " + message.getSender().getName() + " to " + message.getReceiverNames() + " queued");
        } else {
            logMessage("Message from " + message.getSender().getName() + " to " + message.getReceiverNames() + " delivered");
        }
    }

    /**
     * Get clientHandler
     *
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
    public synchronized void deliverUndeliveredMessages(User user) {
        List<Message> messages = offlineMessages.remove(user.getName());
        if (messages != null) {
            for (Message message : messages) {
                ClientHandler handler = getClientHandler(user);
                if (handler != null) {
                    handler.sendMessage(message);

                    // Logga att meddelandet levererades till användaren
                    FileController fileController = new FileController(user);
                    fileController.logMessageReceived(message.getSender().getName(),
                            user.getName(), message.getText());

                    logMessage("Queued message delivered to " + user.getName());
                }
            }
        }
    }

    // Hantera sortering av loggar
    public void handleLogSorting(String criteria) {
        List<String> logLines = readLogFile();
        List<String> sortedLogLines = new ArrayList<>(logLines);

        switch (criteria) {
            case "Time":
                Collections.reverse(sortedLogLines);  // Visa senaste först
                break;
            case "Sender":
                // Implementera sortering efter avsändare
                break;
            case "Receiver":
                // Implementera sortering efter mottagare
                break;
        }

        view.showLogs(sortedLogLines); // Visa sorterade loggar
    }

    // Läs loggfil
    private List<String> readLogFile() {
        // Implementera filinläsning och returnera loggar
        return Collections.emptyList(); // Placeholder
    }

    public void sortLogByTime(String startDateStr, String endDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // Format matchar loggfilen
        List<String> logEntries = new ArrayList<>();

        // Läs in loggfilen
        try (BufferedReader reader = new BufferedReader(new FileReader("res/serverFiles/serverlog.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logEntries.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Konvertera start- och slutdatum från String till Date
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Filtrera och sortera loggposter efter tid
        List<String> filteredEntries = new ArrayList<>();
        for (String entry : logEntries) {
            try {
                String timeStr = entry.substring(entry.lastIndexOf("Time: ") + 6);
                Date logTime = sdf.parse(timeStr);
                if ((startDate == null || logTime.after(startDate)) && (endDate == null || logTime.before(endDate))) {
                    filteredEntries.add(entry);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Sortera de filtrerade loggposterna
        Collections.sort(filteredEntries, (entry1, entry2) -> {
            try {
                String time1 = entry1.substring(entry1.lastIndexOf("Time: ") + 6);
                String time2 = entry2.substring(entry2.lastIndexOf("Time: ") + 6);
                return sdf.parse(time1).compareTo(sdf.parse(time2));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        // Visa de sorterade loggposterna i GUI
        for (String entry : filteredEntries) {
            view.logMessage(entry);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerView serverView = new ServerView();
            Server server = new Server(12345, serverView); // Skapa Server och ServerView
            serverView.setVisible(true);
        });
    }
}
