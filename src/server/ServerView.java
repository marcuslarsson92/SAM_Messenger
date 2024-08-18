package server;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton viewLogsButton;  // Ny knapp för att visa loggar
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

        viewLogsButton = new JButton("View Logs");  // Ny knapp för att visa loggar
        viewLogsButton.addActionListener(e -> openSortCriteriaDialog());

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(viewLogsButton);  // Lägg till knappen till panelen

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

    // Metoder för loggsortering och visning, flyttade från ClientView
    private void openSortCriteriaDialog() {
        String[] options = {"All", "Time", "Sender", "Receiver"};
        String criteria = (String) JOptionPane.showInputDialog(this, "Select sorting criteria:", "Sort Logs",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (criteria != null) {
            switch (criteria) {
                case "All":
                    openLogViewer("All", null, null, null);
                    break;
                case "Time":
                    openDateFilterDialog(criteria);
                    break;
                case "Sender":
                    openSenderReceiverFilterDialog(criteria, "Enter Sender:");
                    break;
                case "Receiver":
                    openSenderReceiverFilterDialog(criteria, "Enter Receiver:");
                    break;
            }
        }
    }

    private void openDateFilterDialog(String criteria) {
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);

        JButton startDateButton = new JButton("Choose Start Date");
        startDateButton.addActionListener(e -> startDateField.setText(showDateTimePicker()));

        JButton endDateButton = new JButton("Choose End Date");
        endDateButton.addActionListener(e -> endDateField.setText(showDateTimePicker()));

        JPanel panel = new JPanel();
        panel.add(new JLabel("Start Date (yyyy-MM-dd HH:mm:ss):"));
        panel.add(startDateField);
        panel.add(startDateButton);
        panel.add(new JLabel("End Date (yyyy-MM-dd HH:mm:ss):"));
        panel.add(endDateField);
        panel.add(endDateButton);

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Date Range", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            openLogViewer(criteria, startDateField.getText(), endDateField.getText(), null);
        }
    }

    private void openSenderReceiverFilterDialog(String criteria, String labelText) {
        JTextField userField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel(labelText));
        panel.add(userField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter " + criteria, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            openLogViewer(criteria, null, null, userField.getText());
        }
    }

    private void openLogViewer(String criteria, String startDate, String endDate, String user) {
        JFrame logFrame = new JFrame("Chat Logs");
        logFrame.setSize(800, 600);
        logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        List<String> logLines = readLogFile();
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);

        if (!criteria.equals("All")) {
            Collections.reverse(logLines);
            sortLog(logLines, criteria, startDate, endDate, user, logArea);
        } else {
            logLines.forEach(line -> logArea.append(line + "\n"));
        }

        logFrame.add(new JScrollPane(logArea), BorderLayout.CENTER);
        logFrame.setVisible(true);
    }

    public void sortLog(List<String> logLines, String criteria, String startDate, String endDate, String user, JTextArea logArea) {
        switch (criteria) {
            case "Time":
                logLines = filterByDate(logLines, startDate, endDate);
                Collections.reverse(logLines);  // Visa senaste först
                break;
            case "Sender":
                logLines = filterBySender(logLines, user);
                break;
            case "Receiver":
                logLines = filterByReceiver(logLines, user);
                break;
        }

        logArea.setText("");
        logLines.forEach(line -> logArea.append(line + "\n"));
    }

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

    public List<String> readLogFile() {
        List<String> logLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("chat_log.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logLines;
    }

    private String showDateTimePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        JComboBox<String> hourComboBox = new JComboBox<>();
        JComboBox<String> minuteComboBox = new JComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourComboBox.addItem(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i++) {
            minuteComboBox.addItem(String.format("%02d", i));
        }

        JPanel timePanel = new JPanel();
        timePanel.add(new JLabel("Hour:"));
        timePanel.add(hourComboBox);
        timePanel.add(new JLabel("Minute:"));
        timePanel.add(minuteComboBox);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(datePicker, BorderLayout.CENTER);
        panel.add(timePanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(null, panel, "Select Date and Time", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String selectedDate = datePicker.getJFormattedTextField().getText();
            String selectedHour = (String) hourComboBox.getSelectedItem();
            String selectedMinute = (String) minuteComboBox.getSelectedItem();
            return selectedDate + " " + selectedHour + ":" + selectedMinute + ":00";
        }
        return "";
    }

    public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerView view = new ServerView();
            view.setVisible(true);
        });
    }
}
