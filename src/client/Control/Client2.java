package client.Control;

import client.Boundary.LoginView;

import javax.swing.*;

public class Client2 {
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                LoginView view = new LoginView();
                view.setVisible(true);
                new LoginController(view);
            });
        }
}
