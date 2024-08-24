package client;

import model.Message;
import model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private Client client;
    private User receiver;
    private JButton attachImageButton; // Knapp för att bifoga bild
    private BufferedImage attachedImage;


    private File attachedImageFile;

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
        attachImageButton = new JButton("Attach Image");
        sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        attachImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attachImage();
            }
        });

        // Använd en horisontell layout för panelen längst ner
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Skapa en panel för knapparna med FlowLayout för att placera dem bredvid varandra
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(attachImageButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);  // Text input field till vänster
        bottomPanel.add(buttonPanel, BorderLayout.EAST);   // Knappar till höger

        add(chatScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            if(client.getUser() != receiver) {
                try {
                    List<User> receivers = new ArrayList<>();
                    receivers.add(receiver);
                    Message message = new Message(client.getUser(), receivers, text, null);
                    client.sendMessage(message);
                    chatArea.append(client.getUser().getName() + ": " + text + "\n");
                    inputField.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private void attachImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                attachedImage = ImageIO.read(file);
                JOptionPane.showMessageDialog(this, "Image attached successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to attach image: " + ex.getMessage());
            }
        }
    }





    public void receiveMessage(Message message) {
        //chatArea.append(message.getSender().getName() + ": " + message.getText() + "\n");

        String text = message.getText();
        chatArea.append(message.getSender().getName() + ": " + text + "\n");
        if (message.getImage() != null) {
            // Visa någon indikation om att en bild är bifogad. Mer avancerat GUI kan visa bilden.
            chatArea.append("[Image attached]\n");
        }
    }

}






/*

import model.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton attachImageButton; // Knapp för att bifoga bild
    private BufferedImage attachedImage;

    public ChatWindow() {
        setTitle("Chat Window");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        inputField = new JTextField(20);
        sendButton = new JButton("Send");
        attachImageButton = new JButton("Attach Image");  // Knapp för att bifoga bild

        panel.add(inputField);
        panel.add(sendButton);
        panel.add(attachImageButton);  // Lägg till knappen i GUI:t
        add(panel, BorderLayout.SOUTH);

        // Lyssnare för att skicka ett meddelande
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Lyssnare för att bifoga en bild
        attachImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attachImage();
            }
        });
    }

    private void sendMessage() {
        String text = inputField.getText();
        Message message = new Message(text, attachedImage);
        displayMessage(message);
        inputField.setText("");
        attachedImage = null;  // Reset image after sending
    }

    private void attachImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                attachedImage = ImageIO.read(file);
                JOptionPane.showMessageDialog(this, "Image attached successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to attach image: " + ex.getMessage());
            }
        }
    }

    private void displayMessage(Message message) {
        String text = message.getText();
        chatArea.append("You: " + text + "\n");
        if (message.getImage() != null) {
            // Visa någon indikation om att en bild är bifogad. Mer avancerat GUI kan visa bilden.
            chatArea.append("[Image attached]\n");
            // För att visa bilden i JTextArea kan du använda en JLabel med en ImageIcon
            ImageIcon icon = new ImageIcon(message.getImage());
            chatArea.insertIcon(icon);
            chatArea.append("\n"); // Ny rad efter bilden
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatWindow chatWindow = new ChatWindow();
            chatWindow.setVisible(true);
        });
    }
}


/*
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatWindow extends JFrame {
    private Client client;
    private User receiver;
    private JTextPane chatPane;
    private JTextField inputField;
    private JButton attachImageButton;
    private JButton sendButton;
    private File attachedImageFile;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    public ChatWindow(Client client, User receiver) {
        this.client = client;
        this.receiver = receiver;

        setTitle("Chat with " + receiver.getName());
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatPane);
        chatScrollPane.setSize(350, 450);
        chatScrollPane.setLocation(0, 30);
        chatScrollPane.setBackground(Color.gray);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        inputField = new JTextField();
        attachImageButton = new JButton("Attach Image");
        sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        attachImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attachImage();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(attachImageButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(chatScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        try {
            String formattedDate = dateFormat.format(new Date());

            // Lägg till avsändarens namn och tid
            chatPane.getDocument().insertString(chatPane.getDocument().getLength(), client.getUser().getName() + " " + formattedDate + ": ", null);

            if (attachedImageFile != null) {
                // Om en bild är bifogad, infoga den i chattfönstret
                chatPane.insertIcon(new ImageIcon(attachedImageFile.getAbsolutePath()));
                chatPane.getDocument().insertString(chatPane.getDocument().getLength(), "\n", null);
                attachedImageFile = null; // Rensa den bifogade bilden
            }

            String message = inputField.getText();
            if (!message.isEmpty()) {
                chatPane.getDocument().insertString(chatPane.getDocument().getLength(), message + "\n", null);
                inputField.setText("");
            }

            chatPane.setCaretPosition(chatPane.getDocument().getLength());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void receiveMessage(String senderName, ImageIcon imageIcon, String textMessage) {
        try {
            String formattedDate = dateFormat.format(new Date());

            // Lägg till avsändarens namn och tid
            chatPane.getDocument().insertString(chatPane.getDocument().getLength(), senderName + " " + formattedDate + ": ", null);

            if (imageIcon != null) {
                // Visa mottagen bild
                chatPane.insertIcon(imageIcon);
                chatPane.getDocument().insertString(chatPane.getDocument().getLength(), "\n", null);
            }

            if (textMessage != null && !textMessage.isEmpty()) {
                // Visa mottagen text
                chatPane.getDocument().insertString(chatPane.getDocument().getLength(), textMessage + "\n", null);
            }

            chatPane.setCaretPosition(chatPane.getDocument().getLength());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void attachImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            attachedImageFile = fileChooser.getSelectedFile();
            System.out.println("Vald bild: " + attachedImageFile.getAbsolutePath());
        }
    }


}

 */
