package ev.koslov.data_exchanging.client;



import ev.koslov.data_exchanging.common.AbstractDataExchanger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

final class ClientDataExchanger extends AbstractDataExchanger<ClientConnection> {
    private ClientEndpoint clientEndpoint;

    protected ClientDataExchanger(ClientEndpoint clientEndpoint) throws IOException {
        this.clientEndpoint = clientEndpoint;
    }

    @Override
    protected void closeConnection(ClientConnection connection) {
        clientEndpoint.shutdown();
    }

    @Override
    protected SelectionKey registerChannel(SocketChannel channel) throws IOException {
        return super.registerChannel(channel);
    }
}
