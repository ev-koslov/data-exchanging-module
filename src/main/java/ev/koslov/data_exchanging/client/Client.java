package ev.koslov.data_exchanging.client;


import ev.koslov.data_exchanging.common.AbstractEndpoint;
import ev.koslov.data_exchanging.common.AbstractClientInterface;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class Client<I extends AbstractClientInterface> extends AbstractEndpoint {
    private ClientConnection connection;
    private I clientInterface;

    public Client(String host, int port, I clientInterface) throws Exception {
        super(clientInterface);
        try {
            this.clientInterface = clientInterface;

            SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));

            ClientDataExchanger dataExchanger = new ClientDataExchanger(this);
            SelectionKey key = dataExchanger.registerChannel(channel);
            ClientMessageParser messageParser = new ClientMessageParser(getReadyMessages());

            connection = new ClientConnection(key, messageParser);

            getExecutorService().execute(dataExchanger);
            getExecutorService().execute(getMessageSorter());

        } catch (Exception e) {
            shutdown();
            throw e;
        }

    }

    public final ClientConnection getConnection() {
        return connection;
    }

    public I getClientInterface() {
        return clientInterface;
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
