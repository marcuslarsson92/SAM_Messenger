package client.Control;

import client.Boundary.ChatView;
import client.Entity.Message;
import client.Entity.User;
import client.Boundary.LoginView;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * The type Client.
 */
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

    /**
     * Instantiates a new Client.
     *
     * @param user          the user
     * @param serverAddress the server address
     * @param serverPort    the server port
     * @throws IOException the io exception
     */
    public Client(User user, String serverAddress, int serverPort) throws IOException {
        this.user = user;
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        // Skicka användarinformation till servern
        out.writeObject(user);
        out.flush();
        new Thread(new Listener()).start();
    }

    /**
     * Sets view.
     *
     * @param view the view
     */
    public void setView(LoginView view) {
        this.view = view;

        // Registrera action listener för anslutningsknappen
        view.setConnectButtonListener(e -> handleLogin());
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Send message.
     *
     * @param message the message
     * @throws IOException the io exception
     */
    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    /**
     * Sets message listener.
     *
     * @param messageListener the message listener
     */
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Sets user list listener.
     *
     * @param userListListener the user list listener
     */
    public void setUserListListener(UserListListener userListListener) {
        this.userListListener = userListListener;
    }

    /**
     * Handle login function
     */

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

        ChatController chatController = new ChatController(this);
        ChatView chatView = new ChatView(chatController);
        chatController.setView(chatView);
        chatView.setVisible(true);
        view.setVisible(false);
    }

    /**
     * Creates file for new user
     * @param userFile
     * @param username
     * @param selectedIcon
     */

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

    /**
     * Load user file
     * @param userFile
     */

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
    /**
     * Listener is a background task that continuously listens for incoming objects
     * from the server over a socket connection. Depending on the type of object received,
     * it triggers the appropriate listener: either to handle a message or to update the user list.
     */
    private class Listener implements Runnable {
        /**
         * Continuously listens for incoming objects from the server,
         * processes them, and delegates to the appropriate listener.
         */
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
