package client.Control;

import client.Boundary.ChatView;
import client.Boundary.LoginView;
import client.Entity.User;

import java.io.IOException;
public class LoginController {
    private LoginView view;
    private Client client;
    private ChatController chatController;

    public LoginController(LoginView view) {
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
            ChatView chatView = new ChatView();
            chatController = new ChatController(client, chatView);

            chatView.setVisible(true);
            view.setVisible(false);
        } catch (IOException ex) {
            ex.printStackTrace();
            view.showErrorMessage("Connection failed");
        }
    }
}

