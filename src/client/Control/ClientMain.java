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
            new ClientViewController(view);
        });
    }
}
