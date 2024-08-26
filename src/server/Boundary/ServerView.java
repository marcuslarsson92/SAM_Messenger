package server.Boundary;

import server.Control.Server;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private Server server;

    public ServerView() {
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

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // GUI-method to log messages
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void startServer() {
        // GUI logic only, server logic is handled in Server class
        server = new Server(12345);
        server.setView(this); // Setting the view so that Server can call logMessage
        new Thread(server::start).start();

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logMessage("Server started at " + LocalDateTime.now());
    }

    private void stopServer() {
        if (server != null) {
            server.stopServer();  // Just calling a method in Server class
            logMessage("Server stopped at " + LocalDateTime.now());
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
}


