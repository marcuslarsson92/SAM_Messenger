package client.Control;

import client.Boundary.ChatView;
import client.Boundary.ClientView;
import client.Control.Client;
import client.Entity.User;

import java.io.IOException;

/*package client.Control;

import client.Boundary.ChatView;
import client.Control.Client;
import client.Entity.User;
import client.Boundary.ClientView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ClientViewController {
    private ClientView view;

    // Katalog för användarfiler
    private final String userFilesDirectory = "res/userFiles/";

    public ClientViewController(ClientView view) {
        this.view = view;

        // Registrerar event listener för "Connect"-knappen
        this.view.setConnectButtonListener(e -> handleLogin());
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

        User user = new User(username, selectedIcon);
        System.out.println("username: " + username + " selected icon: " + selectedIcon);

        try {
            Client client = new Client(user, "localhost", 12345);
            ChatView chatView = new ChatView(client); // Använd din befintliga ChatView-klass här
            chatView.setVisible(true);
            view.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            view.showErrorMessage("Connection failed");
        }
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
}
'
 */
public class ClientViewController {
    private ClientView view;
    private Client client;

    public ClientViewController(ClientView view) {
        this.view = view;
        this.view.setConnectButtonListener(e -> connect());
    }

    private void connect() {
        String username = view.getUsername();
        String selectedIcon = view.getSelectedIcon();

        if (username.isEmpty() || selectedIcon == null) {
            view.showErrorMessage("Please enter a username and select an icon.");
            return;
        }

        User user = new User(username, selectedIcon);
        try {
            client = new Client(user, "localhost", 12345);
            client.setView(view);
            // Öppna ChatView
            ChatView chatView = new ChatView(client);
            chatView.setVisible(true);
            view.setVisible(false);
        } catch (IOException ex) {
            ex.printStackTrace();
            view.showErrorMessage("Connection failed");
        }
    }
}

