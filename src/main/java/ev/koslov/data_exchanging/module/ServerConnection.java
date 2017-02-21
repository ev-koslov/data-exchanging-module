package ev.koslov.data_exchanging.module;


import java.nio.channels.SelectionKey;


public class ServerConnection extends AbstractConnection {
    private final long id;

    protected ServerConnection(SelectionKey selectionKey, AbstractMessageParser messageParser, long connectionId) {
        super(selectionKey, messageParser);
        this.id = connectionId;
    }

    public long getId() {
        return id;
    }
}
