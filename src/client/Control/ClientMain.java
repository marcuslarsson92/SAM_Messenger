package client.Control;

import client.Boundary.ClientView;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientView view = new ClientView();
            view.setVisible(true);
            new ClientViewController(view);
        });
    }
}
