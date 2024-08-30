package client.Control;

import client.Boundary.ChatView;
import client.Boundary.LoginView;
import client.Entity.User;

import java.io.IOException;

/**
 * The type Login controller.
 */
public class LoginController {
    private LoginView view;
    private Client client;
    private ChatController chatController;

    /**
     * Instantiates a new Login controller.
     *
     * @param view the view
     */
    public LoginController(LoginView view) {
        this.view = view;
        this.view.setConnectButtonListener(e -> connect());
    }
    /**
     * Establish a connection to the server using the provided username and selected icon.
     * If successful, a new chat window is opened, and the current view is hidden.
     * If the username or icon is missing, or the connection fails, an error message is shown.
     */
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
            chatController = new ChatController(client);
            ChatView chatView = new ChatView(chatController);
            chatController.setView(chatView);
            chatView.setVisible(true);
            view.setVisible(false);
        } catch (IOException ex) {
            ex.printStackTrace();
            view.showErrorMessage("Connection failed");
        }
    }
}

