package control;

import java.io.IOException;

public class Controller {
    public static void main(String[] args) {
        Server server = new Server(821);
        server.start();
    }
}
