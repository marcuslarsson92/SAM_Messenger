package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientView extends JFrame {
    private JButton connectButton;
    private JLabel statusLabel;
    private JTextField userNameField;
    private String userName;

    public ClientView() {
        setTitle("Client Connection");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userNameField = new JTextField();
        connectButton = new JButton("Connect");
        statusLabel = new JLabel("Not connected");

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = userNameField.getText();
                if (!userName.isEmpty()) {
                    connect();
                } else {
                    statusLabel.setText("Please enter a username");
                }

            }
        });

        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(new JLabel("Please enter username:"));
        panel.add(userNameField);
        panel.add(connectButton);
        panel.add(statusLabel);

        add(panel);
    }
    private void connect() {

        if (userName != null) {
            String iconPath = "res/icons/" + userName + ".png";
            User user = new User(userName, iconPath);
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
