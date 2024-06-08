package server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
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

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void startServer() {
        server = new Server(12345) {
            @Override
            public void logMessage(String message) {
                ServerView.this.logMessage(message);
            }
        };
        serverThread = new Thread(server::start);
        serverThread.start();

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logMessage("Server started at " + LocalDateTime.now());
    }

    private void stopServer() {
        if (server != null) {
            try {
                server.stop();
                serverThread.interrupt();
                logMessage("Server stopped at " + LocalDateTime.now());
            } catch (IOException e) {
                logMessage("Error stopping server: " + e.getMessage());
            }
        }

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerView view = new ServerView();
            view.setVisible(true);
        });
    }
}
