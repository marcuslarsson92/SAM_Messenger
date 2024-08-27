package client.Boundary;
import client.Control.ChatController;
import client.Control.Client;
import client.Entity.User;

import javax.swing.*;
import java.awt.*;

public class ChatView extends JFrame {

    private JList<Object> userList;
    private DefaultListModel<Object> userListModel;
    private ChatController chatController;
    private JLabel loggedInAsLabel;
    private JButton groupChatButton;

    public ChatView(Client client) {
        this.setController(new ChatController(client, this));

        setTitle("Chat Application - " + client.getUser().getName());
        setSize(500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ImageIcon userIcon = client.getUser().getIcon();
        if (userIcon != null) {
            Image scaledImage = userIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            userIcon = new ImageIcon(scaledImage);
        }
        String loggedInText = "Logged in as: " + client.getUser().getName();
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

    public void setController(ChatController chatController) {
        this.chatController = chatController;
    }

    public void clearUserList() {
        userListModel.clear();
    }

    public void addUserListElement(Object element) {
        userListModel.addElement(element);
    }

    public void repaintUserList() {
        userList.repaint();
    }

    private class UserListCellRenderer extends DefaultListCellRenderer {
        private ChatController chatController;

        public UserListCellRenderer(ChatController chatController) {
            this.chatController = chatController;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // Hantera bakgrundsfärg
            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            // Om det är en användare
            if (value instanceof User) {
                User user = (User) value;

                // Skala om ikonen till en mindre storlek
                ImageIcon icon = user.getIcon();
                if (icon != null) {
                    Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImage);
                    label.setIcon(icon);
                }

                label.setText(user.getName());
                label.setHorizontalTextPosition(JLabel.RIGHT);  // Placera texten till höger om bilden

                // Lägg till en checkbox
                JCheckBox contactCheckBox = new JCheckBox();
                contactCheckBox.setSelected(chatController.getContacts().contains(user));
                contactCheckBox.addActionListener(e -> {
                    if (contactCheckBox.isSelected()) {
                        chatController.getContacts().add(user);
                    } else {
                        chatController.getContacts().remove(user);
                    }
                    chatController.updateUserList();
                });

                panel.add(label, BorderLayout.WEST);  // Placera till vänster
                panel.add(contactCheckBox, BorderLayout.EAST);  // Placera checkboxen till höger
                panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));  // Lägg till lite padding

                return panel;

            } else if (value instanceof String) {  // Om det är en sektion ("Online:" eller "Offline:")
                String stringValue = (String) value;
                label.setText(stringValue);
                if (stringValue.equals("Online:") || stringValue.equals("Offline:")) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setHorizontalAlignment(JLabel.LEFT);  // Justera till vänster
                    panel.add(label, BorderLayout.WEST);  // Lägg till etiketten till vänster
                    return panel;
                }
            }

            panel.add(label, BorderLayout.WEST);  // Lägg till etiketten till vänster för övriga strängar
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));  // Lägg till lite padding
            return panel;
        }
    }
}


