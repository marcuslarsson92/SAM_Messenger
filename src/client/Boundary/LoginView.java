package client.Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * The type Login view.
 */
public class LoginView extends JFrame {
    private JTextField usernameField;
    private JRadioButton[] iconButtons;
    private JButton connectButton;
    private String[] iconPaths = {
            "icons/icon1.png",
            "icons/icon2.png",
            "icons/icon3.png",
            "icons/icon4.png"
    };

    /**
     * Instantiates a new Login view.
     */
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
    /**
     * Creates ImageIcon from given search path.
     *
     * @return ImageIcon
     */
    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource("/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return usernameField.getText().trim();
    }

    /**
     * Gets selected icon.
     *
     * @return the selected icon
     */
    public String getSelectedIcon() {
        for (int i = 0; i < iconButtons.length; i++) {
            if (iconButtons[i].isSelected()) {
                return iconPaths[i];
            }
        }
        return null;
    }

    /**
     * Sets connect button listener.
     *
     * @param listener the listener
     */
    public void setConnectButtonListener(ActionListener listener) {
        connectButton.addActionListener(listener);
    }

    /**
     * Show error message.
     *
     * @param message the message
     */
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
