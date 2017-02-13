package ev.koslov.data_exchanging.server;


import ev.koslov.data_exchanging.common.AbstractConnection;
import ev.koslov.data_exchanging.common.AbstractMessageParser;

import java.nio.channels.SelectionKey;


public class ServerConnection extends AbstractConnection {
    private long id;

    protected ServerConnection(SelectionKey selectionKey, AbstractMessageParser messageParser) {
        super(selectionKey, messageParser);
    }

    final void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    protected void close() {
        super.close();
    }
}
