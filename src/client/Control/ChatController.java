package client.Control;

import client.Boundary.ChatView;
import client.Boundary.ChatWindow;
import client.Entity.Message;
import client.Entity.User;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ChatController {

    private Client client;
    private ChatView chatView;

    private List<User> users;
    private DefaultListModel<Object> newMessageUsers;
    private Map<String, ChatWindow> chatWindows;
    private Set<User> contacts;

    public ChatController(Client client, ChatView chatView) {
        this.client = client;
        this.chatView = chatView;
        this.users = new ArrayList<>();
        this.newMessageUsers = new DefaultListModel<>();
        this.chatWindows = new HashMap<>();
        this.contacts = new HashSet<>();

        this.chatView.setChatController(this);

        client.setMessageListener(this::handleIncomingMessage);
        client.setUserListListener(updatedUsers -> {
            users = updatedUsers;
            updateUserList();
        });
    }

    public void handleUserSelection(Object selectedValue) {
        if (selectedValue instanceof User) {
            User selectedUser = (User) selectedValue;
            if (newMessageUsers.contains(selectedUser)) {
                newMessageUsers.removeElement(selectedUser);
                chatView.repaintUserList();
            }
            openChatWindow(selectedUser.getName());
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

    public void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            chatView.clearUserList();

            // Lägg till sektion för online användare
            chatView.addUserListElement("Online:");
            for (User user : users) {
                if (!user.getName().equals(client.getUser().getName())) {
                    chatView.addUserListElement(user);
                }
            }

            // Lägg till ett tomt utrymme för separation
            chatView.addUserListElement(" ");

            // Lägg till sektion för offline användare
            chatView.addUserListElement("Offline:");
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
                    chatView.addUserListElement(username);
                }
            }
        });
    }

    public ChatWindow openChatWindow(String username) {
        User user = getUserByName(username);
        if (user != null) {
            return chatWindows.computeIfAbsent(username, k -> {
                ChatWindow chatWindow = new ChatWindow(client, user);

                Point mainWindowLocation = chatView.getLocation();
                int mainWindowWidth = chatView.getWidth();
                int mainWindowHeight = chatView.getHeight();

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
            chatView.repaintUserList();
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

    public Client getClient() {
        return client;
    }

    public Set<User> getContacts() {
        return contacts;
    }

    public void setView(ChatView chatvView) {
        this.chatView = chatView;
    }

    public DefaultListModel<Object> getNewMessageUsers() {
        return newMessageUsers;
    }

    public void newGroupChat() {
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
}
