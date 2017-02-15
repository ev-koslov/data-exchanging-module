package ev.koslov.data_exchanging.client;


import ev.koslov.data_exchanging.common.AbstractEndpoint;
import ev.koslov.data_exchanging.common.AbstractClientInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class Client extends AbstractEndpoint {
    private ClientConnection connection;

    public Client(String host, int port, AbstractClientInterface clientInterface) throws IOException {
        super(clientInterface);
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));

            ClientDataExchanger dataExchanger = new ClientDataExchanger(this);
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

}
