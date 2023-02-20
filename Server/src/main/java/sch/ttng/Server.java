package sch.ttng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final Logger logger = Logger.getLogger(Server.class.getName());

    private Socket socket = null;
    private ServerSocket server = null;

    public Server(int port) {
        try {
            logger.setLevel(Level.ALL);
            server = new ServerSocket(port);
            logger.log(Level.INFO, "Server started, waiting for client");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to create ServerSocket", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to start server: " + e.getMessage());
        }
    }

    public void start() {
        BufferedReader readerIn = null;
        while (true) {
            try {
                socket = server.accept();
                readerIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                logger.log(Level.INFO, "Created buffered input stream");
                listen(readerIn, new ServerListener() {
                    @Override
                    public void onMessage(String message) {
                        System.out.println("-> " + message);
                    }
                });
            } catch (SocketException e) {
                logger.log(Level.INFO, "Closing the connection");
                return;
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
                return;
            } finally {
                try {
                    socket.close();
                    server.close();
                    readerIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void listen(BufferedReader readerIn, ServerListener listener) throws IOException {
        String line = null;
        while ((line = readerIn.readLine()) != null) {
            if ("close".equals(line)) {
                listener.onMessage(line);
                throw new SocketException("Close signal received");
            }
            listener.onMessage(line);
        }
    }

}
