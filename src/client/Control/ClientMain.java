package client.Control;

import client.Boundary.ClientView;
import client.Boundary.ChatView;
import client.Entity.User;

import javax.swing.*;
import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientView view = new ClientView();
            view.setVisible(true);

            // Koppla login-knappen till logik
            view.setConnectButtonListener(e -> {
                String username = view.getUsername();
                String selectedIcon = view.getSelectedIcon();

                if (username.isEmpty() || selectedIcon == null) {
                    view.showErrorMessage("Please enter a username and select an icon.");
                    return;
                }

                // Skapa användarobjekt med angivna uppgifter
                User user = new User(username, selectedIcon);

                // Skapa klienten med användarinformationen
                try {
                    Client client = new Client(user, "localhost", 12345);
                    client.setView(view);

                    // Starta chatvyn
                    ChatView chatView = new ChatView(client);
                    chatView.setVisible(true);
                    view.setVisible(false);  // Dölj inloggningsfönstret
                } catch (IOException ex) {
                    ex.printStackTrace();
                    view.showErrorMessage("Connection failed");
                }
            });
        });
    }
}
