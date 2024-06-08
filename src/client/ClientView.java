package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientView extends JFrame {
    private JComboBox<String> userComboBox;
    private JButton connectButton;
    private JLabel statusLabel;

    public ClientView() {
        setTitle("Client Connection");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] userNames = {"Alexandra", "Simon", "Marcus", "Johan"};
        userComboBox = new JComboBox<>(userNames);
        connectButton = new JButton("Connect");
        statusLabel = new JLabel("Not connected");

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(new JLabel("Choose user:"));
        panel.add(userComboBox);
        panel.add(connectButton);
        panel.add(statusLabel);

        add(panel);
    }

    private void connect() {
        String username = (String) userComboBox.getSelectedItem();
        String iconPath = "res/icons/" + username.toLowerCase() + ".png";

        if (username != null) {
            User user = new User(username, iconPath);
            try {
                Client client = new Client(user, "localhost", 12345);
                ChatView chatView = new ChatView(client);
                chatView.setVisible(true);
                this.setVisible(false);
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Connection failed");
            }
        } else {
            statusLabel.setText("Please choose a user");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientView view = new ClientView();
            view.setVisible(true);
        });
    }
}
