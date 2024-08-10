package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private JButton connectButton;

    // NYTT: Knapp för att skapa en gruppchatt
    private JButton createGroupChatButton;

    public ClientView() {
        setTitle("Client Connection");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Användarnamnspanel
        JPanel usernamePanel = new JPanel(new BorderLayout());
        JLabel usernameLabel = new JLabel("Enter username: ");
        usernamePanel.add(usernameLabel, BorderLayout.WEST);

        usernameField = new JTextField();
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        add(usernamePanel, BorderLayout.NORTH);

        // Ikonvalspanel
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        JLabel iconLabel = new JLabel("Choose icon: ");
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconPanel.add(iconLabel);

        JPanel iconButtonPanel = new JPanel(new FlowLayout());
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

        // Anslutningsknapp och statuslabel
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> handleLogin());

        // NYTT: Initialisering av gruppchattknappen
        createGroupChatButton = new JButton("Create Group Chat"); // NYTT: Skapa en ny knapp för att initiera en gruppchatt
        createGroupChatButton.addActionListener(e -> handleCreateGroupChat()); // NYTT: Lägg till en ActionListener för att hantera gruppchattens skapande

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.add(connectButton);
        bottomPanel.add(createGroupChatButton); // NYTT: Lägg till gruppchattknappen till bottenpanelen

        add(bottomPanel, BorderLayout.SOUTH);
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
        String username = usernameField.getText().trim();
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

        User user = new User(username, selectedIcon);
        try {
            Client client = new Client(user, "localhost", 12345);
            ChatView chatView = new ChatView(client);
            chatView.setVisible(true);
            this.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            //statusLabel.setText("Connection failed");
        }
    }

    // NYTT: Metod för att hantera skapandet av en gruppchatt
    private void handleCreateGroupChat() {
        String username = usernameField.getText().trim();
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

        User user = new User(username, selectedIcon);
        try {
            Client client = new Client(user, "localhost", 12345);
            GroupChatCreationView groupChatView = new GroupChatCreationView(client); // NYTT: Öppnar ett nytt fönster för att skapa en gruppchatt
            groupChatView.setVisible(true);
            this.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientView view = new ClientView();
            view.setVisible(true);
        });
    }
}
