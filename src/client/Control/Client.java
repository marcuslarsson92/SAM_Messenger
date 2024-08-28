package client.Control;

import client.Boundary.ChatView;
import client.Entity.Message;
import client.Entity.User;
import client.Boundary.LoginView;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    private User user;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessageListener messageListener;
    private UserListListener userListListener;
    private LoginView view;
    private ChatController chatController;
    private final String userFilesDirectory = "res/userFiles/";

    public Client(User user, String serverAddress, int serverPort) throws IOException {
        this.user = user;
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        // Skicka användarinformation till servern
        out.writeObject(user);

        new Thread(new Listener()).start();
    }

    public void setView(LoginView view) {
        this.view = view;

        // Registrera action listener för anslutningsknappen
        view.setConnectButtonListener(e -> handleLogin());
    }

    public User getUser() {
        return user;
    }

    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setUserListListener(UserListListener userListListener) {
        this.userListListener = userListListener;
    }

    private void handleLogin() {
        String username = view.getUsername();
        String selectedIcon = view.getSelectedIcon();

        if (username.isEmpty() || selectedIcon == null) {
            view.showErrorMessage("Please enter a username and select an icon.");
            return;
        }

        // Kontrollera om filen med samma användarnamn redan finns
        File userFile = new File(userFilesDirectory + username + ".txt");
        createUserFile(userFile, username, selectedIcon);

        user = new User(username, selectedIcon);
        System.out.println("username: " + username + " selected icon: " + selectedIcon);

        ChatController chatController = new ChatController(this, null);
        ChatView chatView = new ChatView();
        chatController.setView(chatView);
        chatView.setChatController(chatController);
        chatView.setVisible(true);
        view.setVisible(false);
    }

    private void createUserFile(File userFile, String username, String selectedIcon) {
        try {
            // Kontrollera att katalogen finns, annars skapa den
            File directory = new File(userFilesDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (userFile.createNewFile()) {
                System.out.println("File created: " + userFile.getName());
                FileWriter writer = new FileWriter(userFile);
                writer.write("Username: " + username + "\n");
                writer.write("Iconpath: " + selectedIcon + "\n");
                writer.write("Activity Log:\n");
                writer.close();
            } else {
                loadUserFile(userFile);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();
        }
    }

    private void loadUserFile(File userFile) {
        try {
            Scanner scanner = new Scanner(userFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();
        }
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                Object obj;
                while ((obj = in.readObject()) != null) {
                    if (obj instanceof Message) {
                        if (messageListener != null) {
                            messageListener.onMessageReceived((Message) obj);
                        }
                    } else if (obj instanceof List) {
                        if (userListListener != null) {
                            userListListener.onUserListUpdated((List<User>) obj);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
