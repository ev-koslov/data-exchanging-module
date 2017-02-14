package ev.koslov.data_exchanging.client;



import ev.koslov.data_exchanging.common.AbstractConnection;
import ev.koslov.data_exchanging.common.AbstractMessageParser;

import java.nio.channels.SelectionKey;

/**
 * Implementation of {@link AbstractConnection} class. User for client-side connections.
 */
public final class ClientConnection extends AbstractConnection {

    protected ClientConnection(SelectionKey selectionKey, AbstractMessageParser messageParser) {
        super(selectionKey, messageParser);
    }

    @Override
    protected void close() {
        super.close();
    }
}
