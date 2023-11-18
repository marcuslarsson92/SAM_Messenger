package client;

import model.User;
import model.Message;
import server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Client {
    private User user;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(user);
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<User> getUserList() {
        try {
            return (List<User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage(User recipient, String message) {
        try {
            out.writeObject(new Message(user, recipient, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientView clientView = new ClientView();
    }
}
