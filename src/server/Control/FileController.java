package server.Control;

import client.Entity.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type File controller.
 */
public class FileController {

    private File logFile;

    /**
     * Instantiates a new File controller.
     *
     * @param user the user
     */
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

    public FileController() {
        logFile = new File("res/serverFiles/serverlog.txt");

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

    /**
     * Log message received.
     *
     * @param sender   the sender
     * @param receiver the receiver
     * @param message  the message
     */
// Loggar ett mottaget meddelande
    public void logMessageReceived(String sender, String receiver, String message) {
        System.out.println("Logging received message for: " + receiver);
        String logEntry = formatLogEntry(sender, receiver, message, "received");
        writeToFile(logEntry);
    }

    /**
     * Formats a log entry string based on the sender, receiver, message content, and action type.
     * The action can be either "sent" or "received", which determines how the log entry is phrased.
     * The log entry includes a timestamp of when the action occurred.
     *
     * @param sender The name of the user sending the message.
     * @param receiver The name of the user receiving the message.
     * @param message The content of the message.
     * @param action The action performed, either "sent" or "received".
     * @return A formatted log entry string or an empty string if the action is not recognized.
     */
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

    /**
     * Writes a log entry to the specified log file.
     * The log entry is appended to the file, creating it if it doesn't exist.
     *
     * @param logEntry The log entry to write to the file.
     */
    private void writeToFile(String logEntry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
