package client;

import model.Message;
import model.User;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatViewWindow extends JFrame {
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private Client client;
    private List<User> users;
    private JLabel loggedInAsLabel;
    private DefaultListModel<String> newMessageUsers;
    private JTextArea chatArea;
    private Map<String, User> chatUsers; // To keep track of chat context

    public ChatViewWindow(Client client) {
        this.client = client;
        this.users = new ArrayList<>();
        this.newMessageUsers = new DefaultListModel<>();
        this.chatUsers = new HashMap<>();

        client.setMessageListener(message -> handleIncomingMessage(message));
        client.setUserListListener(updatedUsers -> {
            users = updatedUsers;
            updateUserList();
        });

        setTitle("Chat Application - " + client.getUser().getName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize user list and model
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null && !selectedUser.equals("Online:") && !selectedUser.equals("Offline:")) {
                    if (newMessageUsers.contains(selectedUser)) {
                        newMessageUsers.removeElement(selectedUser);
                        userList.repaint();
                    }
                    loadChatForUser(selectedUser);
                }
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(200, 600));

        // Chat area and input
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userScrollPane, chatPanel);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);

        loggedInAsLabel = new JLabel("Logged in as: " + client.getUser().getName());
        add(loggedInAsLabel, BorderLayout.NORTH);

        updateUserList();
    }

    private void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            userListModel.addElement("Online:");
            List<String> offlineUsers = new ArrayList<>();
            for (User user : getAllUsers()) {
                if (users.contains(user)) {
                    if (!user.getName().equals(client.getUser().getName())) {
                        userListModel.addElement(user.getName());
                    }
                } else {
                    offlineUsers.add(user.getName());
                }
            }
            userListModel.addElement("");
            userListModel.addElement("Offline:");
            for (String offlineUser : offlineUsers) {
                userListModel.addElement(offlineUser);
            }
        });
    }

    private void sendMessage() {
        String text = inputField.getText();
        String selectedUser = userList.getSelectedValue();
        if (!text.isEmpty() && selectedUser != null && !selectedUser.equals("Online:") && !selectedUser.equals("Offline:")) {
            try {
                User receiver = chatUsers.get(selectedUser);
                List<User> receivers = new ArrayList<>();
                receivers.add(receiver);
                Message message = new Message(client.getUser(), receivers, text, null);
                client.sendMessage(message);
                chatArea.append("Me: " + text + "\n");
                inputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleIncomingMessage(Message message) {
        String senderName = message.getSender().getName();
        chatArea.append(senderName + ": " + message.getText() + "\n");

        if (!newMessageUsers.contains(senderName)) {
            newMessageUsers.addElement(senderName);
            userList.repaint();
        }
        playNotificationSound();
    }

    private void loadChatForUser(String username) {
        User user = getUserByName(username);
        if (user != null) {
            chatUsers.put(username, user);
            chatArea.setText("Chat with " + username + ":\n");
        }
    }

    private User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    private List<User> getAllUsers() {
        // Replace this method with actual logic to fetch users
        List<User> allUsers = new ArrayList<>();
        allUsers.add(new User("Alexandra", "res/icons/alexandra.png"));
        allUsers.add(new User("Simon", "res/icons/simon.png"));
        allUsers.add(new User("Marcus", "res/icons/marcus.png"));
        allUsers.add(new User("Johan", "res/icons/johan.png"));
        return allUsers;
    }

    private void playNotificationSound() {
        try {
            File soundFile = new File("res/sound/notification.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (newMessageUsers.contains(value)) {
                setBackground(Color.BLUE);
                setForeground(Color.WHITE);
            }
            if (value.toString().equals("Offline:")) {
                setBackground(Color.LIGHT_GRAY);
                setForeground(Color.DARK_GRAY);
            }
            return component;
        }
    }

}



