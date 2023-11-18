package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ClientView extends JFrame implements ActionListener {
    private JTextField nameField;
    private JComboBox<Icon> iconBox;
    private JList<User> userList;
    private DefaultListModel<User> listModel;
    private JButton chatButton;
    private User selectedUser;
    private Client client;

    public ClientView() {
        super("Messenger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel userPanel = new JPanel(new BorderLayout());

        nameField = new JTextField("Enter your name");
        nameField.addActionListener(this);
        userPanel.add(nameField, BorderLayout.NORTH);

        Icon[] icons = loadIcons();
        iconBox = new JComboBox<>(icons);
        iconBox.addActionListener(this);
        userPanel.add(iconBox, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> updateSelectedUser());
        JScrollPane scrollPane = new JScrollPane(userList);
        userPanel.add(scrollPane, BorderLayout.SOUTH);

        chatButton = new JButton("Start Chat");
        chatButton.setEnabled(false);
        chatButton.addActionListener(this);
        add(chatButton, BorderLayout.SOUTH);

        add(userPanel, BorderLayout.WEST);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Icon[] loadIcons() {
        Icon[] icons = new Icon[4];
        for (int i = 0; i < icons.length; i++) {
            String filename = "/icons/icon" + (i + 1) + ".png";
            ImageIcon icon = new ImageIcon(getClass().getResource(filename));
            icons[i] = icon;
        }
        return icons;
    }

    private void connectToServer() {
        String name = nameField.getText();
        Icon icon = (Icon) iconBox.getSelectedItem();
        User user = new User(name, "/res/icons/icon1");
        client = new Client(user);
        client.connectToServer();
        List<User> userList = client.getUserList();
        for (User u : userList) {
            listModel.addElement(u);
        }
        chatButton.setEnabled(true);
    }

    private void updateSelectedUser() {
        selectedUser = userList.getSelectedValue();
    }

    private void startChat() {
        if (selectedUser != null) {
            ChatView chatView = new ChatView(client, selectedUser);
            chatView.setVisible(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nameField || e.getSource() == iconBox) {
            connectToServer();
        } else if (e.getSource() == chatButton) {
            startChat();
        }
    }
}

