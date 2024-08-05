package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientView extends JFrame {
    private JTextField usernameField;
    private JRadioButton[] iconButtons;
    private String[] iconPaths = {
            "icons/icon1.png",
            "icons/icon2.png",
            "icons/icon3.png",
            "icons/icon4.png"
    };

    public ClientView() {
        setTitle("Client Connection");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BorderLayout());

        JLabel usernameLabel = new JLabel("Enter username: ");
        usernamePanel.add(usernameLabel, BorderLayout.WEST);

        usernameField = new JTextField();
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        add(usernamePanel, BorderLayout.NORTH);

        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("Choose icon: ");
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconPanel.add(iconLabel);

        JPanel iconButtonPanel = new JPanel();
        iconButtonPanel.setLayout(new FlowLayout()); 
        iconButtons = new JRadioButton[iconPaths.length];
        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < iconPaths.length; i++) {
            iconButtons[i] = new JRadioButton();
            ImageIcon icon = createImageIcon(iconPaths[i]);
            if (icon != null) {
                iconButtons[i].setIcon(icon);
            }
            group.add(iconButtons[i]);
            iconButtonPanel.add(iconButtons[i]);
        }

        iconPanel.add(iconButtonPanel);
        add(iconPanel, BorderLayout.CENTER);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();

            }
        });

        add(connectButton, BorderLayout.SOUTH);
    }

    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource("/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String selectedIcon = null;

        for (int i = 0; i < iconButtons.length; i++) {
            if (iconButtons[i].isSelected()) {
                selectedIcon = iconPaths[i];
                break;
            }
        }

        if (username.isEmpty() || selectedIcon == null) {
            JOptionPane.showMessageDialog(this, "Please enter a username and select an icon.");
            return;
        }

        System.out.println("Username: " + username);
        System.out.println("Selected Icon: " + selectedIcon);

        User user = new User(username, selectedIcon);
        try {
            Client client = new Client(user, "localhost", 12345);
            System.out.println("Client: " + username + selectedIcon);
            ChatView chatView = new ChatView(client);
            chatView.setVisible(true);
            this.setVisible(false);


        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connection failed.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientView().setVisible(true);
            }
        });
    }
}






