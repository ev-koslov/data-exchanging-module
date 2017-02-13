package ev.koslov.data_exchanging.server;


import ev.koslov.data_exchanging.common.AbstractConnection;
import ev.koslov.data_exchanging.common.AbstractMessageParser;

import java.nio.channels.SelectionKey;


public class ServerConnection extends AbstractConnection {
    private long id;

    protected ServerConnection(SelectionKey selectionKey, AbstractMessageParser messageParser, long connectionId) {
        super(selectionKey, messageParser);
        this.id = connectionId;
    }

    public long getId() {
        return id;
    }

    @Override
    protected void close() {
        super.close();
    }
}
