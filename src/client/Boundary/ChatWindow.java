package client.Boundary;

import client.Control.ChatController;
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
import java.io.File;
import java.util.List;

/**
 * The type Chat window.
 */
public class ChatWindow extends JFrame {
    private JTextPane chatPane;
    private JTextField inputField;
    private JButton sendButton;
    private JButton attachImageButton;
    private ChatController controller;
    private List<User> receivers;
    private ImageIcon attachedImage;

    /**
     * Instantiates a new Chat window.
     *
     * @param controller the controller
     * @param client     the client
     * @param receiver   the receiver
     */
    public ChatWindow(ChatController controller, Client client, User receiver) {
        this(controller, client, List.of(receiver));
    }

    /**
     * Instantiates a new Chat window.
     *
     * @param controller the controller
     * @param client     the client
     * @param receivers  the receivers
     */
    public ChatWindow(ChatController controller, Client client, List<User> receivers) {
        this.controller = controller;
        this.receivers = receivers;

        setTitle(receivers.size() == 1 ? "Chat with " + receivers.get(0).getName() : "Group chat with " + getReceiverNames());
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatPane);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            controller.handleSendMessage(this, inputField.getText());
            inputField.setText(""); // Töm inputfältet efter att meddelandet har skickats
        });

        attachImageButton = new JButton("Attach Image");
        attachImageButton.addActionListener(e -> controller.attachImage(this));


        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(attachImageButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);


        add(chatScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Display message.
     *
     * @param message the message
     */
    public void displayMessage(Message message) {
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


    /**
     * Display image.
     *
     * @param sender the sender
     * @param image  the image
     */
    public void displayImage(String sender, ImageIcon image) {
        StyledDocument doc = chatPane.getStyledDocument();

        try {
            // Lägg till avsändarens namn
            doc.insertString(doc.getLength(), sender + ": ", null);

            // Skala om bilden
            Image scaledImage = image.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            // Skapa och använd en stil för att infoga den skalade bilden
            Style style = chatPane.addStyle("ImageStyle", null);
            StyleConstants.setIcon(style, scaledIcon);

            // Infoga ett mellanslag efter namnet och sedan bilden på samma rad
            doc.insertString(doc.getLength(), " ", null);
            doc.insertString(doc.getLength(), " ", style);

            // Lägg till en ny rad efter bilden
            doc.insertString(doc.getLength(), "\n", null);

            // Scrolla till botten
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    /**
     * Show file chooser file.
     *
     * @return the file
     */
    public File showFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
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

    /**
     * Receive message.
     *
     * @param message the message
     */
    public void receiveMessage(Message message) {
        displayMessage(message);
    }

    /**
     * Gets receivers.
     *
     * @return the receivers
     */
    public List<User> getReceivers() {
        return receivers;
    }
}


