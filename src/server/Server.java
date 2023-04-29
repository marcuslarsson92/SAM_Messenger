package server;
import model.User;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<ClientHandler> clients = new ArrayList<>();
    private ServerView serverView;
    private int port;

    public Server(int port) {
        this.port = port;
        serverView = new ServerView(this);
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        for (ClientHandler clientHandler : clients) {
            userList.add(clientHandler.getUser());
        }
        return userList;
    }

    public void broadcastMessage(ClientHandler sender, User recipient, String message) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUser().equals(recipient)) {
                clientHandler.sendMessage(sender.getUser(), message);
                break;
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(5000);
        server.start();
    }
}
