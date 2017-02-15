package ev.koslov.data_exchanging.client;



import ev.koslov.data_exchanging.common.AbstractDataExchanger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

final class ClientDataExchanger extends AbstractDataExchanger<ClientConnection> {
    private final Client client;

    protected ClientDataExchanger(Client client) throws IOException {
        this.client = client;
    }

    @Override
    protected void closeConnection(ClientConnection connection) {
        client.shutdown();
    }

    @Override
    protected SelectionKey registerChannel(SocketChannel channel) throws IOException {
        return super.registerChannel(channel);
    }
}
