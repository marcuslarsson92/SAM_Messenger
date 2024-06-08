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

public class ChatView extends JFrame {
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private Client client;
    private List<User> users;
    private JLabel loggedInAsLabel;
    private DefaultListModel<String> newMessageUsers;
    private Map<String, ChatWindow> chatWindows;

    public ChatView(Client client) {
        this.client = client;
        this.users = new ArrayList<>();
        this.newMessageUsers = new DefaultListModel<>();
        this.chatWindows = new HashMap<>();

        client.setMessageListener(message -> handleIncomingMessage(message));
        client.setUserListListener(updatedUsers -> {
            users = updatedUsers;
            updateUserList();
        });

        setTitle("Chat Application - " + client.getUser().getName());
        setSize(300, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loggedInAsLabel = new JLabel("Logged in as: " + client.getUser().getName());
        inputField = new JTextField();
        sendButton = new JButton("Send");

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (newMessageUsers.contains(selectedUser)) {
                    newMessageUsers.removeElement(selectedUser);
                    userList.repaint();
                }
                openChatWindow(selectedUser);
            }
        });

        userList.setCellRenderer(new UserListCellRenderer());

        JScrollPane userScrollPane = new JScrollPane(userList);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(loggedInAsLabel, BorderLayout.NORTH);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);
        leftPanel.add(panel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.CENTER);
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty() && userList.getSelectedValue() != null) {
            try {
                User receiver = getUserByName(userList.getSelectedValue());
                List<User> receivers = new ArrayList<>();
                receivers.add(receiver);
                Message message = new Message(client.getUser(), receivers, text, null);
                client.sendMessage(message);
                inputField.setText("");
                openChatWindow(receiver.getName()).receiveMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private List<User> getAllUsers() {
        // This method should return the list of all users (both online and offline)
        // For demonstration purposes, let's assume it returns a hardcoded list
        List<User> allUsers = new ArrayList<>();
        allUsers.add(new User("Alexandra", "res/icons/alexandra.png"));
        allUsers.add(new User("Simon", "res/icons/simon.png"));
        allUsers.add(new User("Marcus", "res/icons/marcus.png"));
        allUsers.add(new User("Johan", "res/icons/johan.png"));
        return allUsers;
    }

    private ChatWindow openChatWindow(String username) {
        User user = getUserByName(username);
        if (user != null) {
            return chatWindows.computeIfAbsent(username, k -> {
                ChatWindow chatWindow = new ChatWindow(client, user);
                chatWindow.setVisible(true);
                return chatWindow;
            });
        }
        return null;
    }

    private void handleIncomingMessage(Message message) {
        String senderName = message.getSender().getName();
        ChatWindow chatWindow = openChatWindow(senderName);
        if (chatWindow != null) {
            chatWindow.receiveMessage(message);
        }

        if (!newMessageUsers.contains(senderName)) {
            newMessageUsers.addElement(senderName);
            userList.repaint();
        }
        playNotificationSound();
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
