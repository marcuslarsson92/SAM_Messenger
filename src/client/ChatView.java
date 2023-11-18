package client;

import model.Message;
import model.MessageListener;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ChatView extends JFrame implements ActionListener, MessageListener {
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private User user;
    private User chatPartner;
    private Client client;

    public ChatView(Client client, User chatPartner) {
        super("Chat with " + chatPartner.getName());
        this.user = client.getUser();
        this.chatPartner = chatPartner;
        this.client = client;
        //client.addMessageListener(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(this);
        messagePanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(this);
        messagePanel.add(sendButton, BorderLayout.EAST);

        add(messagePanel, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void sendMessage() {
        /*
        String text = messageField.getText();
        if (!text.isEmpty()) {
            Message message = new Message(user, chatPartner, text);
            try {
                client.sendMessage(user, message);
                addMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageField.setText("");
        }

         */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == messageField) {
            sendMessage();
        } else if (e.getSource() == sendButton) {
            sendMessage();
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        /*
        if ((message.getFromUser().equals(user) && message.getToUser().equals(chatPartner))
                || (message.getFromUser().equals(chatPartner) && message.getToUser().equals(user))) {
            addMessage(message);
        }

         */
    }

    private void addMessage(Message message) {
        /*
        String text = message.getFromUser().getName() + ": " + message.getText() + "\n";
        messageArea.append(text);
        */
    }

}
