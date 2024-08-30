package server.Control;

import client.Entity.Message;
import client.Entity.User;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User user;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Initialisera strömmar
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Läs in användaren
            user = (User) in.readObject();
            server.addUser(user);

            // Leverera eventuella tidigare ej levererade meddelanden
            server.deliverUndeliveredMessages(user);

            Object obj;
            while ((obj = in.readObject()) != null) {
                if (obj instanceof Message) {
                    Message message = (Message) obj;
                    message.setDeliveredTime(null); // Nollställ leveranstiden för ny leverans
                    server.sendMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // När klienten kopplar från, ta bort användaren från servern och stäng anslutningen
            server.removeUser(user);
            closeConnection();
        }
    }

    /**
     * Skickar listan över användare till klienten.
     *
     * @param userList Listan över användare som ska skickas.
     */
    public void sendUserList(List<User> userList) {
        try {
            out.writeObject(userList);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Skickar ett meddelande till klienten.
     *
     * @param message Meddelandet som ska skickas.
     */
    public void sendMessage(Message message) {
        try {
            // Sätt leveranstid till nuvarande tid
            message.setDeliveredTime(java.time.LocalDateTime.now());
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hämtar användaren som är associerad med denna ClientHandler.
     *
     * @return Användarobjektet.
     */
    public User getUser() {
        return user;
    }

    /**
     * Stänger anslutningen till klienten.
     */
    void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
