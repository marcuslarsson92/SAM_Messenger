package client.Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JRadioButton[] iconButtons;
    private JButton connectButton;

    // Ikonbanor
    private String[] iconPaths = {
            "icons/icon1.png",
            "icons/icon2.png",
            "icons/icon3.png",
            "icons/icon4.png"
    };

    public LoginView() {
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

        // Anslutningsknapp
        connectButton = new JButton("Connect");
        add(connectButton, BorderLayout.SOUTH);
    }

    // Skapar en ImageIcon från en given sökväg
    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource("/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    // Returnerar användarnamn
    public String getUsername() {
        return usernameField.getText().trim();
    }

    // Returnerar vald ikon
    public String getSelectedIcon() {
        for (int i = 0; i < iconButtons.length; i++) {
            if (iconButtons[i].isSelected()) {
                return iconPaths[i];
            }
        }
        return null;
    }

    // Sätter action listener för connect-knappen
    public void setConnectButtonListener(ActionListener listener) {
        connectButton.addActionListener(listener);
    }

    // Visar ett felmeddelande
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
