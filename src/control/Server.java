package control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run () {
        Socket socket = null;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                socket = serverSocket.accept();
                new ClientHandler(socket);
            }
        } catch (Exception e) {}
    }

    private class ClientHandler extends Thread {
        Socket socket;
        ObjectOutputStream oos;
        ObjectInputStream ois;

        public ClientHandler (Socket socket) throws IOException {
            this.socket = socket;
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        }

    }
}
