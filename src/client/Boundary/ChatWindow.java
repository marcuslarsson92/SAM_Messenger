package client.Boundary;


import client.Control.Client;
import client.Entity.Message;
import client.Entity.User;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChatWindow extends JFrame {
    private JTextPane chatPane;
    private JTextField inputField;
    private JButton sendButton;
    private Client client;
    private List <User> receivers;
    private JButton attachImageButton;
    private ImageIcon attachedImage;
    private File attachedImageFile;
    private JScrollPane chatScrollPane;

    public ChatWindow(Client client, User receiver) {
        this(client, List.of(receiver));
    }
    public ChatWindow(Client client, List <User> receivers) {
        this.client = client;
        this.receivers = receivers;

        if (receivers.size() == 1) {
            setTitle("Chat with " + receivers.get(0).getName());
        } else {
            setTitle("Group chat with " + getReceiverNames());
        }

        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Istället för att stänga, bara dölja fönstret
                setVisible(false);
            }
        });

        // Initialisera chatPane och lägg till den i chatScrollPane
        chatPane = new JTextPane();
        chatPane.setEditable(false);

        chatScrollPane = new JScrollPane(chatPane);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        inputField = new JTextField();
        attachImageButton = new JButton("Attach Image");
        sendButton = new JButton("Send");

        sendButton.addActionListener(e -> sendMessage());
        attachImageButton.addActionListener(e -> attachImage());

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
        String text = inputField.getText();

        if (!text.isEmpty()) {
            Message message = new Message(client.getUser(), receivers, text, null);
            try {
                client.sendMessage(message);
                displayMessage(message);
                inputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (attachedImageFile != null) {
            sendImage(attachedImage);
        }
    }

    private void attachImage() {
        // Skapa en JFileChooser
        JFileChooser fileChooser = new JFileChooser();

        // Få referens till "Pictures"-mappen i användarens filsystem
        File picturesDir = FileSystemView.getFileSystemView().getDefaultDirectory();
        File picturesFolder = new File(picturesDir, "Pictures");

        // Sätt standardkatalogen till "Pictures"-mappen
        fileChooser.setCurrentDirectory(picturesFolder);

        // Visa dialogen och hantera användarens val
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            attachedImageFile = fileChooser.getSelectedFile();
            try {
                attachedImage = new ImageIcon(attachedImageFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Image attached successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to attach image: " + ex.getMessage());
            }
        }
    }

    private void sendImage(ImageIcon imageIcon) {

        Message message = new Message(client.getUser(), receivers, null, attachedImage);
        try {
            client.sendMessage(message);
            displayMessage(message);
            attachedImageFile = null;
            attachedImage = null;
            inputField.setText("");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(Message message) {
        displayMessage(message);
    }
    private void displayMessage(Message message) {
        StyledDocument doc = chatPane.getStyledDocument();

        try {
            // Lägg till avsändarens namn
            doc.insertString(doc.getLength(), message.getSender().getName() + ": ", null);

            if (message.getImage() != null) {
                // Anta att message.getImage() returnerar en ImageIcon
                ImageIcon icon = (ImageIcon) message.getImage();
                Image img = icon.getImage(); // Få bilden från ImageIcon

                // Skala bilden
                Image scaledImg = img.getScaledInstance(115, 115, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImg);

                // Skapa och använd en stil för att infoga bilden
                Style style = chatPane.addStyle("ImageStyle", null);
                StyleConstants.setIcon(style, scaledIcon);

                // Infoga ett mellanslag efter namnet och sedan bilden på samma rad
                doc.insertString(doc.getLength(), " ", null);
                doc.insertString(doc.getLength(), " ", style);

                // Lägg till en ny rad efter bilden
                doc.insertString(doc.getLength(), "\n", null);

            } else if (message.getText() != null) {
                // Lägg till textmeddelandet om ingen bild finns
                doc.insertString(doc.getLength(), message.getText() + "\n", null);
            }

            chatPane.setCaretPosition(doc.getLength()); // Scrolla till botten

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Hämta namn på alla mottagare för att visa i fönstertiteln
    private String getReceiverNames() {
        StringBuilder names = new StringBuilder();
        for (User user : receivers) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(user.getName());
        }
        return names.toString();
    }

}
