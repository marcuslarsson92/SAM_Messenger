package client;

import model.Message;
import model.User;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatView extends JFrame {

    private JList<User> userList;
    private DefaultListModel<User> userListModel;
    private Client client;
    private List<User> users;
    private JLabel loggedInAsLabel;
    private DefaultListModel<User> newMessageUsers;
    private Map<String, ChatWindow> chatWindows;
    private JButton groupChatButton;

    public ChatView(Client client) {
        this.client = client;
        this.users = new ArrayList<>();
        this.newMessageUsers = new DefaultListModel<>();
        this.chatWindows = new HashMap<>();

        client.setMessageListener(this::handleIncomingMessage);
        client.setUserListListener(updatedUsers -> {
            users = updatedUsers;
            updateUserList();
        });

        setTitle("Chat Application - " + client.getUser().getName());
        setSize(500, 800);  // Ändra fönstrets storlek här
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Uppdaterad del för att visa ikon bredvid text
        ImageIcon userIcon = client.getUser().getIcon();
        if (userIcon != null) {
            Image scaledImage = userIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            userIcon = new ImageIcon(scaledImage);
        }
        String loggedInText = "Logged in as: " + client.getUser().getName();
        loggedInAsLabel = new JLabel(loggedInText, userIcon, JLabel.LEFT);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selectedUser = userList.getSelectedValue();
                if (newMessageUsers.contains(selectedUser)) {
                    newMessageUsers.removeElement(selectedUser);
                    userList.repaint();
                }
                openChatWindow(selectedUser.getName());
            }
        });

        userList.setCellRenderer(new UserListCellRenderer());

        groupChatButton = new JButton("DM all online");
        groupChatButton.addActionListener(e -> newGroupChat());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(groupChatButton);

        JScrollPane userScrollPane = new JScrollPane(userList);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(loggedInAsLabel, BorderLayout.NORTH);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.CENTER);
    }

    /**
     * Opens new window with a groupchat
     */
    private void newGroupChat() {
        // Samla alla online-användare
        List<User> onlineUsers = new ArrayList<>();
        for (User user : users) {
            if (!user.getName().equals(client.getUser().getName())) {
                onlineUsers.add(user);
            }
        }

        // Skapa ett nytt gruppchattfönster
        ChatWindow groupChatWindow = new ChatWindow(client, onlineUsers);
        groupChatWindow.setVisible(true);
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
            for (User user : users) {
                if (!user.getName().equals(client.getUser().getName())) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    private ChatWindow openChatWindow(String username) {
        User user = getUserByName(username);
        if (user != null) {
            return chatWindows.computeIfAbsent(username, k -> {
                ChatWindow chatWindow = new ChatWindow(client, user);

                // Hämta dimensioner och position för ChatView
                Point mainWindowLocation = this.getLocation();
                int mainWindowWidth = this.getWidth();
                int mainWindowHeight = this.getHeight();

                // Hämta dimensioner för ChatWindow
                int chatWindowWidth = chatWindow.getWidth();
                int chatWindowHeight = chatWindow.getHeight();

                // Beräkna position för att centrera ChatWindow
                int chatWindowX = mainWindowLocation.x + (mainWindowWidth - chatWindowWidth) / 2;
                int chatWindowY = mainWindowLocation.y + (mainWindowHeight - chatWindowHeight) / 2;

                // Sätt positionen för ChatWindow
                chatWindow.setLocation(chatWindowX, chatWindowY);

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

        User sender = getUserByName(senderName);
        if (sender != null && !newMessageUsers.contains(sender)) {
            newMessageUsers.addElement(sender);
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
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof User) {
                User user = (User) value;

                // Skala om ikonen till en mindre storlek
                ImageIcon icon = user.getIcon();
                if (icon != null) {
                    Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImage);
                    label.setIcon(icon);
                }

                label.setText(user.getName());
                label.setHorizontalTextPosition(JLabel.RIGHT);  // Placera texten till höger om bilden
            }

            if (newMessageUsers.contains(value)) {
                label.setBackground(Color.BLUE);
                label.setForeground(Color.WHITE);
            } else if (value instanceof String && value.equals("Offline:")) {
                label.setBackground(Color.LIGHT_GRAY);
                label.setForeground(Color.DARK_GRAY);
            }

            return label;
        }
    }
}
