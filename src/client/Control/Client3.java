package client.Control;

import client.Boundary.ClientView;

import javax.swing.*;

public class Client3 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientView view = new ClientView();
            view.setVisible(true);
            new ClientViewController(view);
        });
    }
}

