package server.Boundary;


import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import server.Control.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

/**
 * The type Server view.
 */
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

    /**
     * Log message.
     *
     * @param message the message
     */
// GUI-method to log messages
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
            switch (criteria) {
                case "All":
                case "Sender":
                case "Receiver":
                    JOptionPane.showMessageDialog(this, "At the moment you can only filter by time");
                    break;
                case "Time":
                    openTimeFilterDialog();
                    break;
            }
        }
    }

    private void openTimeFilterDialog() {
            JTextField startDateField = new JTextField(10);
            JTextField endDateField = new JTextField(10);

            JButton startDateButton = new JButton("Choose Start Date");
            startDateButton.addActionListener(e -> startDateField.setText(showDateTimePicker()));

            JButton endDateButton = new JButton("Choose End Date");
            endDateButton.addActionListener(e -> endDateField.setText(showDateTimePicker()));

            JPanel panel = new JPanel();
            panel.add(new JLabel("Start Date:"));
            panel.add(startDateField);
            panel.add(startDateButton);
            panel.add(new JLabel("End Date:"));
            panel.add(endDateField);
            panel.add(endDateButton);

            int result = JOptionPane.showConfirmDialog(this, panel, "Select Date Range", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                server.sortLogByTime(startDateField.getText(), endDateField.getText());
            }

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
}
