package ev.koslov.data_exchanging.server;

import ev.koslov.data_exchanging.common.AbstractDataExchanger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

final class ServerDataExchanger extends AbstractDataExchanger<ServerConnection> {

    private Server server;

    ServerDataExchanger(Server server) throws IOException {
        super();
        this.server = server;
    }

    @Override
    protected SelectionKey registerChannel(SocketChannel channel) throws IOException {
        return super.registerChannel(channel);
    }

    @Override
    protected void closeConnection(ServerConnection connection) {
        server.closeConnection(connection.getId());
    }
}