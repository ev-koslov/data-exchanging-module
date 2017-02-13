package ev.koslov.data_exchanging.server;


import ev.koslov.data_exchanging.common.AbstractEndpoint;
import ev.koslov.data_exchanging.common.ServerInterface;
import ev.koslov.data_exchanging.components.Message;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class ServerEndpoint<C extends ServerConnection, I extends ServerInterface> extends AbstractEndpoint<C, I> {

    private boolean running;
    private ServerDataExchanger dataExchanger;
    private LinkedBlockingQueue<Long> newConnectionIds, closedConnectionIds;

    private Map<Long, C> connections;
    private Class<C> connectionClass;

    public ServerEndpoint(int port, I serverInterface, Class<C> connectionClass) throws IOException {
        super(serverInterface);

        newConnectionIds = new LinkedBlockingQueue<Long>();
        closedConnectionIds = new LinkedBlockingQueue<Long>();
        connections = new HashMap<Long, C>();
        this.connectionClass = connectionClass;

        try {

            NewConnectionsListener newConnectionsListener = new NewConnectionsListener(port);

            dataExchanger = new ServerDataExchanger(this);

            getExecutorService().execute(dataExchanger);
            getExecutorService().execute(newConnectionsListener);
            getExecutorService().execute(getMessageSorter());


        } catch (IOException e) {
            shutdown();
            throw e;
        }

        running = true;
    }

    public final C getConnection(long id) {
        return connections.get(id);
    }

    public final List<C> getConnections() {
        return new ArrayList<C>(connections.values());
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

            if (connections.containsKey(nextMessage.getSourceId())) {
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

        NewConnectionsListener(int port) throws IOException {
            serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(port));
        }

        public void run() {
            try {

                while (!Thread.currentThread().isInterrupted()) {

                    //create SocketChannel instance
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    //generate id for new connection
                    long connectionId = System.nanoTime();
                    //register channel to data exchanger
                    SelectionKey key = dataExchanger.registerChannel(socketChannel);
                    //create message parser instance using connection id and readyMessages queue
                    ServerMessageParser messageParser = new ServerMessageParser(getReadyMessages(), connectionId);

                    //create server connection instance and register it map using its ID
                    C serverConnection = instantiateConnection(connectionClass, key, messageParser);
                    serverConnection.setId(connectionId);

                    connections.put(serverConnection.getId(), serverConnection);
                    newConnectionIds.add(serverConnection.getId());

                }

            } catch (Exception e) {
                e.printStackTrace();
                //TODO: check how to watch for server stops listening for new connetions.

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
}
