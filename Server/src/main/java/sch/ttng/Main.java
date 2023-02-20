package sch.ttng;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Callback");
        logger.setLevel(Level.ALL);
        Server server = new Server(8080);
        server.start();
    }
}