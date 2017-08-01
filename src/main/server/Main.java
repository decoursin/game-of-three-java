package server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Random;

public class Main extends WebSocketServer {

    private static final int PORT = 8777;
    private static final String HOST = "localhost";
    private static final Random random = new Random();
    private static final int MAX = 10000;
    private static final int SUCCESS_STATUS = 0;
    private static final int FAILURE_STATUS = 1;
    private static final String DONE_KEYWORD = "DONE";

    public Main(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("We've connected to the client.\n");

        int n = random.nextInt(MAX) + 1;

        System.out.println("Generated random number: " + n);

        if (n == 1) {
            System.out.println("Winner!!");
            cleanupAndShutdown(conn, SUCCESS_STATUS);
        }

        conn.send(Integer.toString(n));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // If the client closed the connection,
        // we'll shutdown the server.
        System.exit(SUCCESS_STATUS);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message.equals(DONE_KEYWORD)) {
            System.out.println("You've lost, game over.");
            cleanupAndShutdown(conn, SUCCESS_STATUS);
        }

        int current = Integer.parseInt(message);

        System.out.println("Received number: " + current);

        if (current == 1) {
            System.out.println("Winner!!");
            conn.send(DONE_KEYWORD);
            cleanupAndShutdown(conn, SUCCESS_STATUS);
        } else if (current <= 0) {
            System.out.println("Error, something went wrong.");
            cleanupAndShutdown(conn, FAILURE_STATUS);
        }

        String next = Long.toString(Math.round(((double) current) / 3));

        System.out.println("Sending number: " + next);

        conn.send(next);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error with connection " + conn.getRemoteSocketAddress() + ":" + ex);
        cleanupAndShutdown(conn, FAILURE_STATUS);
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully");
    }

    private void cleanupAndShutdown(WebSocket conn, int exitStatus) {
        conn.close();
        System.exit(exitStatus);
    }

    public static void main(String[] args) {
        WebSocketServer server = new Main(new InetSocketAddress(HOST, PORT));
        server.run();
    }
}
