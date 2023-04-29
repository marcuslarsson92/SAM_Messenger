package server;
import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ServerView extends JFrame {
    private JTextArea userTextArea;

    public ServerView(Server server) {
        setTitle("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 300, 300);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        userTextArea = new JTextArea();
        userTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(userTextArea);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JLabel lblNewLabel = new JLabel("Connected users:");
        contentPane.add(lblNewLabel, BorderLayout.NORTH);

        setVisible(true);

        new Thread(() -> {
            while (true) {
                List<User> userList = server.getUserList();
                StringBuilder stringBuilder = new StringBuilder();
                for (User user : userList) {
                    stringBuilder.append(user.getName())
                            .append(" (")
                            .append(user.getIcon())
                            .append(")")
                            .append(System.lineSeparator());
                }
                userTextArea.setText(stringBuilder.toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
