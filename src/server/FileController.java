package server;

import model.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileController {

    private File logFile;

    public FileController(User user) {
        // Sätt filens sökväg till "userFiles" mappen i resources
        String userFilesDir = "res/userFiles/";
        logFile = new File(userFilesDir + user.getName() + ".txt");

        // Kontrollera om filen redan existerar, om inte skapa den
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Loggar ett meddelande skickat av användaren
    public void logMessageSent(String sender, String receiver, String message) {
        System.out.println("Logging sent message for: " + sender);
        String logEntry = formatLogEntry(sender, receiver, message, "sent");
        writeToFile(logEntry);
    }

    // Loggar ett mottaget meddelande
    public void logMessageReceived(String sender, String receiver, String message) {
        System.out.println("Logging received message for: " + receiver);
        String logEntry = formatLogEntry(sender, receiver, message, "received");
        writeToFile(logEntry);
    }

    // Formaterar loggposten
    private String formatLogEntry(String sender, String receiver, String message, String action) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String timestamp = sdf.format(new Date());
        if ("sent".equals(action)) {
            return sender + " sent a message to " + receiver + ". Message: \"" + message + "\" Time: " + timestamp + "\n";
        } else if ("received".equals(action)) {
            return receiver + " received a message from " + sender + ". Message: \"" + message + "\" Time: " + timestamp + "\n";
        }
        return "";
    }

    // Skriver loggposten till filen
    private void writeToFile(String logEntry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

