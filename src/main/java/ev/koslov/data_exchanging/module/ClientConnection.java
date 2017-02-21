package ev.koslov.data_exchanging.module;

import java.nio.channels.SelectionKey;

/**
 * Implementation of {@link AbstractConnection} class. User for client-side connections.
 */
public final class ClientConnection extends AbstractConnection {

    protected ClientConnection(SelectionKey selectionKey, AbstractMessageParser messageParser) {
        super(selectionKey, messageParser);
    }

}
