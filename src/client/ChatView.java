package client;

import client.ChatWindow;
import client.Client;
import model.Message;
import model.User;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ChatView extends JFrame {

    private JList<Object> userList;
    private DefaultListModel<Object> userListModel;
    private Client client;
    private List<User> users;
    private JLabel loggedInAsLabel;
    private DefaultListModel<Object> newMessageUsers;
    private Map<String, ChatWindow> chatWindows;
    private Set<User> contacts;

    public ChatView(Client client) {
        this.client = client;
        this.users = new ArrayList<>();
        this.newMessageUsers = new DefaultListModel<>();
        this.chatWindows = new HashMap<>();
        this.contacts = new HashSet<>();

        client.setMessageListener(this::handleIncomingMessage);
        client.setUserListListener(updatedUsers -> {
            users = updatedUsers;
            updateUserList();
        });

        setTitle("Chat Application - " + client.getUser().getName());
        setSize(500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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
                Object selectedValue = userList.getSelectedValue();
                if (selectedValue instanceof User) {
                    User selectedUser = (User) selectedValue;
                    if (newMessageUsers.contains(selectedUser)) {
                        newMessageUsers.removeElement(selectedUser);
                        userList.repaint();
                    }
                    openChatWindow(selectedUser.getName());
                }
            }
        });

        userList.setCellRenderer(new UserListCellRenderer());

        JScrollPane userScrollPane = new JScrollPane(userList);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(loggedInAsLabel, BorderLayout.NORTH);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);
    }

    private User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    private List<String> loadAllUsernames() {
        File folder = new File("res/userFiles");
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        List<String> usernames = new ArrayList<>();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                String username = file.getName().replace(".txt", "");
                usernames.add(username);
            }
        }
        return usernames;
    }

    private void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();

            // Lägg till sektion för online användare
            userListModel.addElement("Online:");
            for (User user : users) {
                if (!user.getName().equals(client.getUser().getName())) {
                    userListModel.addElement(user);
                }
            }

            // Lägg till ett tomt utrymme för separation
            userListModel.addElement(" ");

            // Lägg till sektion för offline användare
            userListModel.addElement("Offline:");
            List<String> allUsernames = loadAllUsernames();
            for (String username : allUsernames) {
                boolean isOnline = false;
                for (User user : users) {
                    if (user.getName().equals(username)) {
                        isOnline = true;
                        break;
                    }
                }
                if (!isOnline) {
                    userListModel.addElement(username);
                }
            }
        });
    }


    //Ändrat massa strings här om det blir fel, kolla här
    private ChatWindow openChatWindow(String receiver) {
        User recUser = getUserByName(receiver);
        if (recUser != null) {
            return chatWindows.computeIfAbsent(receiver, k -> {
                ChatWindow chatWindow = new ChatWindow(client, recUser);

                Point mainWindowLocation = this.getLocation();
                int mainWindowWidth = this.getWidth();
                int mainWindowHeight = this.getHeight();

                int chatWindowWidth = chatWindow.getWidth();
                int chatWindowHeight = chatWindow.getHeight();

                int chatWindowX = mainWindowLocation.x + (mainWindowWidth - chatWindowWidth) / 2;
                int chatWindowY = mainWindowLocation.y + (mainWindowHeight - chatWindowHeight) / 2;

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
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // Hantera bakgrundsfärg
            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            // Om det är en användare
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

                // Lägg till en checkbox
                JCheckBox contactCheckBox = new JCheckBox();
                contactCheckBox.setSelected(contacts.contains(user));
                contactCheckBox.addActionListener(e -> {
                    if (contactCheckBox.isSelected()) {
                        contacts.add(user);
                    } else {
                        contacts.remove(user);
                    }
                    updateUserList(); // Uppdatera listan efter att checkboxen har klickats
                });

                panel.add(label, BorderLayout.WEST);  // Placera till vänster
                panel.add(contactCheckBox, BorderLayout.EAST);  // Placera checkboxen till höger
                panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));  // Lägg till lite padding

                return panel;

            } else if (value instanceof String) {  // Om det är en sektion ("Online:" eller "Offline:")
                String stringValue = (String) value;
                label.setText(stringValue);
                if (stringValue.equals("Online:") || stringValue.equals("Offline:")) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setHorizontalAlignment(JLabel.LEFT);  // Justera till vänster
                    panel.add(label, BorderLayout.WEST);  // Lägg till etiketten till vänster
                    return panel;
                }
            }

            panel.add(label, BorderLayout.WEST);  // Lägg till etiketten till vänster för övriga strängar
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));  // Lägg till lite padding
            return panel;
        }
    }



}

