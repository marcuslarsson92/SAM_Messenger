package client;

import model.Message;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private Client client;
    private User receiver;

    public ChatWindow(Client client, User receiver) {
        this.client = client;
        this.receiver = receiver;

        setTitle("Chat with " + receiver.getName());
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        add(chatScrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            try {
                List<User> receivers = new ArrayList<>();
                receivers.add(receiver);
                Message message = new Message(client.getUser(), receivers, text, null);
                client.sendMessage(message);
                chatArea.append("Me: " + text + "\n");
                inputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveMessage(Message message) {
        chatArea.append(message.getSender().getName() + ": " + message.getText() + "\n");
    }
}
