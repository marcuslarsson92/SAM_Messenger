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

/**
 * The type Chat controller.
 */
public class ChatController {

    private Client client;
    private ChatView chatView;

    private List<User> users;
    private DefaultListModel<Object> newMessageUsers;
    private Map<String, ChatWindow> chatWindows;
    private Set<User> contacts;
    private ChatWindow chatWindow;

    /**
     * Instantiates a new Chat controller.
     *
     * @param client   the client
     */
    public ChatController(Client client) {
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

    }

    /**
     * Handle user selection.
     *
     * @param selectedValue the selected value
     */
    public void handleUserSelection(Object selectedValue) {
        if (selectedValue instanceof User) {
            User selectedUser = (User) selectedValue;
            openChatWindow(selectedUser.getName());
        }
    }

    /**
     * Open chat window.
     *
     * @param username the username
     * @return
     */

    public ChatWindow openChatWindow(String username) {
        User user = getUserByName(username);
        if (user == null) {
            user = new User(username, null);  // Skapa en ny användare om det behövs
        }

        User finalUser = user;
        ChatWindow chatWindow = chatWindows.computeIfAbsent(username, k -> new ChatWindow(this, client, finalUser));

        chatWindow.setVisible(true);
        return chatWindow;
    }


    /**
     * Get user from given username
     * @param name
     * @return User
     */
    private User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Update user list.
     */
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
                    User offlineUser = new User(username, null);
                    chatView.addUserListElement(offlineUser);
                }
            }
        });
    }

    /**
     * Handle send message.
     *
     * @param chatWindow the chat window
     * @param text       the text
     */
    public void handleSendMessage(ChatWindow chatWindow, String text) {
        if (!text.isEmpty()) {
            Message message = new Message(client.getUser(), chatWindow.getReceivers(), text, null);
            try {
                client.sendMessage(message);
                chatWindow.displayMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all users in the system through their log file
     *
     * @return usernames list of all users in the system
     */

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

    /**
     * Gets client.
     *
     * @return the client
     */
    public Client getClient() {
        return client;
    }

    /**
     * Gets contacts.
     *
     * @return the contacts
     */
    public Set<User> getContacts() {
        return contacts;
    }

    /**
     * Sets view.
     *
     * @param chatView the chat view
     */
    public void setView(ChatView chatView) {
        this.chatView = chatView;
    }

    /**
     * Gets new message users.
     *
     * @return the new message users
     */
    public DefaultListModel<Object> getNewMessageUsers() {
        return newMessageUsers;
    }

    /**
     * New group chat.
     */
    public void newGroupChat() {
        List<User> onlineUsers = new ArrayList<>();
        for (User user : users) {
            if (!user.getName().equals(client.getUser().getName())) {
                onlineUsers.add(user);
            }
        }

        // Skapa ett nytt gruppchattfönster
        ChatWindow groupChatWindow = new ChatWindow(this, client, onlineUsers);
        groupChatWindow.setVisible(true);
    }
    private void handleIncomingMessage(Message message) {
        String senderName = message.getSender().getName();

        // Kontrollera om det redan finns ett chattfönster för avsändaren
        ChatWindow chatWindow = chatWindows.get(senderName);

        if (chatWindow == null) {
            // Om inget chattfönster finns, skapa ett nytt
            User sender = getUserByName(senderName);
            if (sender == null) {
                sender = new User(senderName, null);  // Om användaren inte är känd, skapa en ny User
            }
            chatWindow = new ChatWindow(this, client, sender);
            chatWindows.put(senderName, chatWindow);

            // Centrera fönstret på skärmen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screenSize.width - chatWindow.getWidth()) / 2;
            int y = (screenSize.height - chatWindow.getHeight()) / 2;
            chatWindow.setLocation(x, y);

            // Gör fönstret synligt
            chatWindow.setVisible(true);
        }

        // Visa meddelandet i chattfönstret
        chatWindow.receiveMessage(message);

        // Fokusera fönstret om det inte redan är det
        if (!chatWindow.isFocused()) {
            chatWindow.toFront();
            chatWindow.requestFocus();
        }

        // Om användaren som skickade meddelandet inte redan är i listan över nya meddelandeanvändare, lägg till dem
        if (!newMessageUsers.contains(senderName)) {
            newMessageUsers.addElement(senderName);
            chatView.repaintUserList();  // Uppdatera användarlistan
        }
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


    /**
     * Attach image.
     *
     * @param chatWindow the chat window
     */
    public void attachImage(ChatWindow chatWindow) {
        File imageFile = chatWindow.showFileChooser();
        if (imageFile != null) {
            try {
                ImageIcon imageIcon = new ImageIcon(imageFile.getAbsolutePath());
                Message message = new Message(client.getUser(), chatWindow.getReceivers(), null, imageIcon);
                client.sendMessage(message);
                chatWindow.displayImage(client.getUser().getName(), imageIcon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
