package client.Boundary;

import client.Control.ChatController;
import client.Entity.User;

import javax.swing.*;
import java.awt.*;

/**
 * The type Chat view.
 */
public class ChatView extends JFrame {

    private JList<Object> userList;
    private DefaultListModel<Object> userListModel;
    private JLabel loggedInAsLabel;
    private JButton groupChatButton;
    private ChatController chatController;

    /**
     * Instantiates a new Chat view.
     */
    public ChatView(ChatController chatController) {
        this.chatController = chatController;
        SwingUtilities.invokeLater(() -> {
            initComponents();
            chatController.updateUserList(); // Uppdatera listan efter att komponenterna är initialiserade
            setVisible(true); // Se till att fönstret visas efter att komponenterna har ställts in
        });
    }

    private void initComponents() {
        setTitle("Chat Application - " + chatController.getClient().getUser().getName());
        setSize(500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ImageIcon userIcon = chatController.getClient().getUser().getIcon();
        if (userIcon != null) {
            Image scaledImage = userIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            userIcon = new ImageIcon(scaledImage);
        }
        String loggedInText = "Logged in as: " + chatController.getClient().getUser().getName();
        loggedInAsLabel = new JLabel(loggedInText, userIcon, JLabel.LEFT);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Object selectedValue = userList.getSelectedValue();
                chatController.handleUserSelection(selectedValue);
            }
        });

        groupChatButton = new JButton("DM all online");
        groupChatButton.addActionListener(e -> chatController.newGroupChat());

        userList.setCellRenderer(new UserListCellRenderer(chatController));

        JScrollPane userScrollPane = new JScrollPane(userList);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(groupChatButton);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(loggedInAsLabel, BorderLayout.NORTH);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.CENTER);
    }

    /**
     * Clear user list.
     */
    public void clearUserList() {
        SwingUtilities.invokeLater(() -> userListModel.clear());
    }

    /**
     * Add user list element.
     *
     * @param element the element
     */
    public void addUserListElement(Object element) {
        SwingUtilities.invokeLater(() -> userListModel.addElement(element));
    }

    /**
     * Repaint user list.
     */
    public void repaintUserList() {
        SwingUtilities.invokeLater(() -> userList.repaint());
    }

    /**
     * Custom renderer for cells in the user list, enhancing the display with user icons,
     * names, and a checkbox for selecting contacts. Adjusts appearance based on selection
     * and focus, and handles user interactions through the ChatController.
     */
    private class UserListCellRenderer extends DefaultListCellRenderer {
        private ChatController chatController;

        /**
         * Instantiates a new User list cell renderer.
         *
         * @param chatController the chat controller
         */
        public UserListCellRenderer(ChatController chatController) {
            this.chatController = chatController;
        }

        /**
         * Customizes the appearance and behavior of a list cell in the user list.
         *
         * @param list The JList we're painting.
         * @param value The value returned by list.getModel().getElementAt(index).
         * @param index The cell's index.
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return The component that the renderer uses to draw the value.
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            if (value instanceof User) {
                User user = (User) value;

                ImageIcon icon = user.getIcon();
                if (icon != null) {
                    Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImage);
                    label.setIcon(icon);
                }

                label.setText(user.getName());
                label.setHorizontalTextPosition(JLabel.RIGHT);

                panel.add(label, BorderLayout.WEST);
                panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

                return panel;

            } else if (value instanceof String) {
                String stringValue = (String) value;
                label.setText(stringValue);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.LEFT);
                panel.add(label, BorderLayout.WEST);
                return panel;
            }

            panel.add(label, BorderLayout.WEST);
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            return panel;
        }
    }
}
