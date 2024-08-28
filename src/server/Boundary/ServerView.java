package server.Boundary;

import server.Control.Server;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * The type Server view.
 */
public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private Server server;

    /**
     * Instantiates a new Server view.
     *
     * @param server the server
     */
    public ServerView(Server server) {
        this.server = server;
        setTitle("Server Log");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> server.start());  // Delegate to Server class
        stopButton.addActionListener(e -> server.stopServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Log message.
     *
     * @param message the message
     */
// GUI-method to log messages
    public void logMessage(String message) {
        logArea.append(message + "\n");
    }

}