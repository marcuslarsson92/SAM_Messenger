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

public class Server {
    private final int port;
    private Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>();
    private Queue<Message> undeliveredMessages = new ConcurrentLinkedQueue<>();
    private boolean running = true;
    private ServerView view;
    private List<User> allUsers = new ArrayList<>(); // Lista över alla registrerade användare

    public Server(int port, ServerView view) { // Modifierad konstruktor
        this.port = port;
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

    public void stop() {
        running = false;
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.closeConnection();
        }
        logMessage("Server stopped");
    }

    public void logMessage(String message) {
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

    synchronized void sendMessage(Message message) {
        boolean delivered = false;

        // Skapa en FileController för avsändaren och servern
        FileController senderFileController = new FileController(message.getSender());
        FileController serverFileController = new FileController();

        senderFileController.logMessageSent(message.getSender().getName(), message.getReceiverNames(), message.getText());
        serverFileController.logMessageSent(message.getSender().getName(), message.getReceiverNames(),message.getText());

        for (User receiver : message.getReceivers()) {
            ClientHandler handler = getClientHandler(receiver);
            if (handler != null) {
                handler.sendMessage(message);
                delivered = true;

                // Skapa en FileController för mottagaren
                FileController receiverFileController = new FileController(receiver);
                receiverFileController.logMessageReceived(message.getSender().getName(), receiver.getName(), message.getText());
            }
        }

        if (!delivered) {
            undeliveredMessages.add(message);
            logMessage("Message from " + message.getSender().getName() + " to " + message.getReceiverNames() + " queued");
        } else {
            logMessage("Message from " + message.getSender().getName() + " to " + message.getReceiverNames() + " delivered");
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

    // Hantera sortering av loggar
    public void handleLogSorting(String criteria) {
        List<String> logLines = readLogFile();
        List<String> sortedLogLines = new ArrayList<>(logLines);

        switch (criteria) {
            case "Time":
                //sortedLogLines = filterByDate(sortedLogLines, ...); // Lägg till nödvändiga parametrar
                Collections.reverse(sortedLogLines);  // Visa senaste först
                break;
            case "Sender":
                //sortedLogLines = filterBySender(sortedLogLines, ...);
                break;
            case "Receiver":
                //sortedLogLines = filterByReceiver(sortedLogLines, ...);
                break;
        }

        view.showLogs(sortedLogLines); // Visa sorterade loggar
    }

    // Läs loggfil
    private List<String> readLogFile() {
        // Implementera filinläsning och returnera loggar
        return Collections.emptyList(); // Placeholder
    }

    // Filtreringsmetoder
    private List<String> filterByDate(List<String> logLines, String startDateTime, String endDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = startDateTime.isEmpty() ? LocalDateTime.MIN : LocalDateTime.parse(startDateTime, formatter);
        LocalDateTime end = endDateTime.isEmpty() ? LocalDateTime.MAX : LocalDateTime.parse(endDateTime, formatter);

        List<String> filtered = new ArrayList<>();
        for (String line : logLines) {
            String dateTimePart = line.split("\\|")[0].trim(); // Extrahera hela tidstämpeln (yyyy-MM-dd HH:mm:ss)
            LocalDateTime logDateTime = LocalDateTime.parse(dateTimePart, formatter);
            if (!logDateTime.isBefore(start) && !logDateTime.isAfter(end)) {
                filtered.add(line);
            }
        }
        return filtered;
    }

    private List<String> filterBySender(List<String> logLines, String sender) {
        if (sender == null || sender.isEmpty()) return logLines;
        List<String> filtered = new ArrayList<>();
        for (String line : logLines) {
            if (extractSender(line).equalsIgnoreCase(sender)) {
                filtered.add(line);
            }
        }
        return filtered;
    }

    private List<String> filterByReceiver(List<String> logLines, String receiver) {
        if (receiver == null || receiver.isEmpty()) return logLines;
        List<String> filtered = new ArrayList<>();
        for (String line : logLines) {
            if (extractReceiver(line).equalsIgnoreCase(receiver)) {
                filtered.add(line);
            }
        }
        return filtered;
    }

    private String extractSender(String logLine) {
        return logLine.split("\\|")[1].trim().replace("From: ", "");
    }

    private String extractReceiver(String logLine) {
        return logLine.split("\\|")[2].trim().replace("To: ", "");
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
        Collections.sort(filteredEntries, new Comparator<String>() {
            @Override
            public int compare(String entry1, String entry2) {
                try {
                    String time1 = entry1.substring(entry1.lastIndexOf("Time: ") + 6);
                    String time2 = entry2.substring(entry2.lastIndexOf("Time: ") + 6);
                    return sdf.parse(time1).compareTo(sdf.parse(time2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
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
