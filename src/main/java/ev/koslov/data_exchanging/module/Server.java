package ev.koslov.data_exchanging.module;


import ev.koslov.data_exchanging.components.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public final class Server extends AbstractEndpoint {

    private boolean running;

    private final LinkedBlockingQueue<Long> newConnectionIds, closedConnectionIds;
    private final Map<Long, ServerConnection> connections;

    Server(int port, ServerInterface serverInterface) throws IOException {
        super(serverInterface);
        newConnectionIds = new LinkedBlockingQueue<Long>();
        closedConnectionIds = new LinkedBlockingQueue<Long>();
        connections = new HashMap<Long, ServerConnection>();

        try {

            ServerEmbeddedDataExchanger dataExchanger = new ServerEmbeddedDataExchanger();
            NewConnectionsListener newConnectionsListener = new NewConnectionsListener(port, dataExchanger);

            executeTask(dataExchanger);
            executeTask(newConnectionsListener);

        } catch (IOException e) {
            shutdown();
            throw e;
        }

        running = true;
    }

    public final ServerConnection getConnection(long id) {
        return connections.get(id);
    }

    public final List<ServerConnection> getConnections() {
        return new ArrayList<ServerConnection>(connections.values());
    }

    public final void closeConnection(long id) {
        if (connections.containsKey(id)) {
            connections.remove(id).close();
            closedConnectionIds.add(id);
        }
    }

    @Override
    protected final Message getNextReadyMessage() throws InterruptedException {
        do {
            Message nextMessage = super.getNextReadyMessage();

            if (connections.containsKey(nextMessage.getHeader().getSourceId())) {
                return nextMessage;
            }
        } while (true);
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && running;
    }

    @Override
    public void shutdown() {
        if (!running) {
            return;
        }

        super.shutdown();

        for (ServerConnection serverConnection : connections.values()) {
            serverConnection.close();
        }

        connections.clear();

        running = false;

    }


    private final class NewConnectionsListener implements Runnable {
        private ServerSocketChannel serverSocketChannel;
        private ServerEmbeddedDataExchanger serverDataExchanger;

        NewConnectionsListener(int port, ServerEmbeddedDataExchanger dataExchanger) throws IOException {
            serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(port));
            serverDataExchanger = dataExchanger;
        }

        public void run() {
            try {

                while (!Thread.currentThread().isInterrupted()) {

                    //create SocketChannel instance
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    //generate id for new connection
                    long connectionId = System.nanoTime();
                    //register channel to data exchanger
                    SelectionKey key = serverDataExchanger.registerChannel(socketChannel);
                    //create message parser instance using connection id and readyMessages queue
                    ServerMessageParser messageParser = new ServerMessageParser(getReadyMessages(), connectionId);

                    //create server connection instance and register it map using its ID
                    ServerConnection serverConnection = new ServerConnection(key, messageParser, connectionId);

                    connections.put(serverConnection.getId(), serverConnection);
                    newConnectionIds.add(serverConnection.getId());

                }

            } catch (IOException e) {
                e.printStackTrace();
                //TODO: check how to watch for server stops listening for new connections.

            } finally {
                try {
                    //close serverSocketChannel after task interruption
                    serverSocketChannel.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }


    private final class ServerEmbeddedDataExchanger extends AbstractDataExchanger<ServerConnection>{

        ServerEmbeddedDataExchanger() throws IOException {
            super();
        }

        @Override
        void closeConnection(ServerConnection connection) {
            Server.this.closeConnection(connection.getId());
        }
    }
}
