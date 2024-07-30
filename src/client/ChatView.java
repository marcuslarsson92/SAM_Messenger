package client;

import model.Message;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatView extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private Client client;
    private List<User> users;

    public ChatView(Client client) {
        this.client = client;
        this.users = new ArrayList<>();
        client.setMessageListener(message -> {
            String msg = message.getSender().getName() + ": " + message.getText();
            if (message.getImage() != null) {
                msg += " [Image attached]";
            }
            chatArea.append(msg + "\n");
        });

        
        client.setUserListListener(updatedUsers -> {
            users = updatedUsers;
            updateUserList();
        });


        setTitle("Chat Application | " + client.getUser().getName());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userScrollPane, chatScrollPane);
        splitPane.setDividerLocation(150);

        add(splitPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty() && userList.getSelectedValue() != null) {
            try {
                User receiver = getUserByName(userList.getSelectedValue());
                List<User> receivers = new ArrayList<>();
                receivers.add(receiver);
                Message message = new Message(client.getUser(), receivers, text, null);
                client.sendMessage(message);
                inputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }


    private void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (User user : users) {
                if(!user.getName().equals(client.getUser().getName())) {
                    userListModel.addElement(user.getName());
                }
            }
        });
    }
}
