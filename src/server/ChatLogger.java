package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger {
    private static final String LOG_FILE_PATH = "chat_log.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void logMessage(String sender, String receiver, String message) {
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                String timeStamp = LocalDateTime.now().format(formatter);
                writer.write(timeStamp + " | From: " + sender + " | To: " + receiver + " | Message: " + message);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not log message: " + e.getMessage());
        }
    }
}

