package ev.koslov.data_exchanging.module;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public final class Client extends AbstractEndpoint {
    private ClientConnection connection;

    Client(String host, int port, ClientInterface clientInterface) throws IOException {
        super(clientInterface);
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));

            ClientDataExchanger dataExchanger = new ClientDataExchanger();
            SelectionKey key = dataExchanger.registerChannel(channel);
            ClientMessageParser messageParser = new ClientMessageParser(getReadyMessages());

            connection = new ClientConnection(key, messageParser);

            executeTask(dataExchanger);

        } catch (IOException e) {
            shutdown();
            throw e;
        }

    }

    public final ClientConnection getConnection() {
        return connection;
    }

    @Override
    public final boolean isRunning() {
        return super.isRunning() && connection.isConnected();
    }

    @Override
    public void shutdown() {
        if (connection != null && connection.isConnected()) {
            connection.close();
        }
        super.shutdown();
    }

    private final class ClientDataExchanger extends AbstractDataExchanger<ClientConnection> {

        ClientDataExchanger() throws IOException {
            super();
        }

        @Override
        void closeConnection(ClientConnection connection) {
            Client.this.shutdown();
        }
    }

}
