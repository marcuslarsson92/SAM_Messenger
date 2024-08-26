package server.Control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileController {

    private File logFile;

    public FileController(String username) {
        // Sätt filens sökväg till "userFiles" mappen i resources
        String userFilesDir = "resources/userFiles/";
        logFile = new File(userFilesDir + username + ".txt");

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
        String logEntry = formatLogEntry(sender, receiver, message, "Sent");
        writeToFile(logEntry);
    }

    // Loggar ett meddelande mottaget av användaren
    public void logMessageReceived(String sender, String receiver, String message) {
        String logEntry = formatLogEntry(sender, receiver, message, "Received");
        writeToFile(logEntry);
    }

    // Formaterar loggposten
    private String formatLogEntry(String sender, String receiver, String message, String status) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        String timestamp = sdf.format(new Date());
        return sender + " " + status + " message to " + receiver + ". Message: \"" + message + "\" " + status + ": " + timestamp + "\n";
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
