package server;

import model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private User user;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public User getUser() {
        return user;
    }

    public void sendMessage(User sender, String message) {
        out.println(sender.getName() + ": " + message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Enter your name:");
            String name = in.readLine();
            out.println("Choose your icon:");
            String iconName = in.readLine();

            user = new User(name, iconName);
            out.println("Welcome, " + user.getName() + "!");

            while (true) {
                String input = in.readLine();
                if (input == null) {
                    break;
                }
                server.broadcastMessage(this, user, input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
        }
    }
}
