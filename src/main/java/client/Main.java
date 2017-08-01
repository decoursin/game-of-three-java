package client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class Main extends WebSocketClient {

    private static final int SUCCESS_STATUS = 0;
    private static final int FAILURE_STATUS = 1;
    private static final int WAIT_TIME_IN_MILLISECONDS = 4000;
    private static final String DONE_KEYWORD = "DONE";
    private static final String ADDRESS = "ws://localhost:8887";

    private static final Draft draft = new Draft_6455();

    public Main(URI serverUri, Draft draft) {
        super(serverUri, draft, null, 999);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("We've connected to the Server.\n");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(String message) {
        if (message.equals(DONE_KEYWORD)) {
            System.out.println("You've lost, game over.");
            cleanupAndShutdown(SUCCESS_STATUS);
        }

        int current = Integer.parseInt(message);

        System.out.println("Received number: " + current);

        if (current == 1) {
            System.out.println("Winner!!");
            this.send(DONE_KEYWORD);
            cleanupAndShutdown(SUCCESS_STATUS);
        } else if (current <= 0) {
            System.out.println("Error, something went wrong.");
            cleanupAndShutdown(FAILURE_STATUS);
        }

        String next = Long.toString(Math.round(((double) current) / 3));

        System.out.println("Sending number: " + next);

        this.send(next);
    }

    @Override
    public void onError(Exception ex) {
        if (!(ex instanceof java.net.ConnectException)) {
            this.close();
            System.exit(FAILURE_STATUS);
        }
    }

    private void cleanupAndShutdown(int exitStatus) {
        this.close();
        System.exit(exitStatus);
    }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        System.out.println("This is the client.");

        while (true) {
            WebSocketClient client = new Main(new URI(ADDRESS), draft);
            synchronized (client) {
                if (client.connectBlocking()) {
                    break;
                } else {
                    System.out.println("Could not connect to server, waiting " + WAIT_TIME_IN_MILLISECONDS + " ms.");
                    client.wait(WAIT_TIME_IN_MILLISECONDS);
                }
            }
        }
    }
}