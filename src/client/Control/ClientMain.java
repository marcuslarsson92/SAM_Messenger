// src/client/Control/ClientMain.java
package client.Control;

import client.Boundary.LoginView;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
            new LoginController(loginView);
        });
    }
}
