package server.Boundary;


import server.Control.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton viewLogsButton;
    private Server server;
    private Thread serverThread;

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

        viewLogsButton = new JButton("View Logs");
        viewLogsButton.addActionListener(e -> openSortCriteriaDialog());

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(viewLogsButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void startServer() {
        server = new Server(12345, this); // Skicka referens till ServerView
        serverThread = new Thread(server::start);
        serverThread.start();

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logMessage("Server started at " + LocalDateTime.now());
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            serverThread.interrupt();
            logMessage("Server stopped at " + LocalDateTime.now());
        }

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    // Visa dialog för att välja sorteringskriterier
    private void openSortCriteriaDialog() {
        String[] options = {"All", "Time", "Sender", "Receiver"};
        String criteria = (String) JOptionPane.showInputDialog(this, "Select sorting criteria:", "Sort Logs",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (criteria != null) {
            server.handleLogSorting(criteria); // Flytta logik för att hantera sortering till Server
        }
    }

    // Metod för att visa loggar efter att de sorterats i Server-klassen
    public void showLogs(List<String> logLines) {
        JFrame logFrame = new JFrame("Chat Logs");
        logFrame.setSize(800, 600);
        logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logLines.forEach(line -> logArea.append(line + "\n"));

        logFrame.add(new JScrollPane(logArea), BorderLayout.CENTER);
        logFrame.setVisible(true);
    }
}
