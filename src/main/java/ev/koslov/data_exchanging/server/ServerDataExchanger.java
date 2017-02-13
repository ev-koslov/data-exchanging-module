package ev.koslov.data_exchanging.server;

import ev.koslov.data_exchanging.common.AbstractDataExchanger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

final class ServerDataExchanger extends AbstractDataExchanger<ServerConnection> {

    private ServerEndpoint serverEndpoint;

    ServerDataExchanger(ServerEndpoint serverEndpoint) throws IOException {
        super();
        this.serverEndpoint = serverEndpoint;
    }

    @Override
    protected SelectionKey registerChannel(SocketChannel channel) throws IOException {
        return super.registerChannel(channel);
    }

    @Override
    protected void closeConnection(ServerConnection connection) {
        serverEndpoint.closeConnection(connection.getId());
    }
}