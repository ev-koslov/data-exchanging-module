package ev.koslov.data_exchanging.client;


import ev.koslov.data_exchanging.common.AbstractEndpoint;
import ev.koslov.data_exchanging.common.ClientInterface;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class ClientEndpoint<C extends ClientConnection, I extends ClientInterface> extends AbstractEndpoint<C, I> {
    private C connection;

    public ClientEndpoint(String host, int port, I clientInterface, Class<C> connectionClass) throws Exception {
        super(clientInterface);
        try {

            SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));

            ClientDataExchanger dataExchanger = new ClientDataExchanger(this);
            SelectionKey key = dataExchanger.registerChannel(channel);
            ClientMessageParser messageParser = new ClientMessageParser(getReadyMessages());

            connection = instantiateConnection(connectionClass, key, messageParser);

            getExecutorService().execute(dataExchanger);
            getExecutorService().execute(getMessageSorter());

        } catch (Exception e) {
            shutdown();
            throw e;
        }

    }

    public final C getConnection() {
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
