package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

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

    // Katalog för användarfiler
    private final String userFilesDirectory = "res/userFiles/";

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

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.add(connectButton);

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

        // Kontrollera om filen med samma användarnamn redan finns
        File userFile = new File(userFilesDirectory + username + ".txt");
        createUserFile(userFile, username, selectedIcon);


        User user = new User(username, selectedIcon);
        System.out.println("username: " + username + " selected icon: " + selectedIcon);

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

    private void createUserFile(File userFile, String username, String selectedIcon) {
        try {
            // Kontrollera att katalogen finns, annars skapa den
            File directory = new File(userFilesDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (userFile.createNewFile()) {
                System.out.println("File created: " + userFile.getName());
                FileWriter writer = new FileWriter(userFile);
                writer.write("Username: " + username + "\n");
                writer.write("Iconpath: " + selectedIcon + "\n");
                writer.write("Activity Log:\n");
                writer.close();
            } else {
                loadUserFile(userFile);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();
        }
    }

    private void loadUserFile(File userFile) {
        try {
            Scanner scanner = new Scanner(userFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //När det finns i Server lägg: logMessage(line)
                System.out.println(line);
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file.");
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
