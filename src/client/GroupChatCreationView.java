package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GroupChatCreationView extends JFrame {
    private Client client;
    private JList<User> userList;
    private DefaultListModel<User> userListModel;
    private JButton createButton;
    private JButton cancelButton;

    public GroupChatCreationView(Client client) {
        this.client = client;
        setTitle("Create Group Chat");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Tillåter flera val

        // Lägg till alla tillgängliga användare i listmodellen
        for (User user : client.getAllUsers()) {
            if (!user.getName().equals(client.getUser().getName())) { // Exkludera den inloggade användaren själv
                userListModel.addElement(user);
            }
        }

        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);

        createButton = new JButton("Create");
        createButton.addActionListener(e -> handleCreateGroupChat());

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose()); // Stänger fönstret utan att skapa gruppchatten

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createButton);
        buttonPanel.add(cancelB
