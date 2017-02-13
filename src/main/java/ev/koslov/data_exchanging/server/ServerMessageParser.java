package ev.koslov.data_exchanging.server;

import ev.koslov.data_exchanging.common.AbstractMessageParser;
import ev.koslov.data_exchanging.components.Message;

import java.util.concurrent.LinkedBlockingQueue;


public final class ServerMessageParser  extends AbstractMessageParser {
    private long associatedConnectionId;

    ServerMessageParser(LinkedBlockingQueue<Message> readyMessages, long associatedConnectionId) {
        super(readyMessages);
        this.associatedConnectionId = associatedConnectionId;
    }

    @Override
    protected Message prepareNewMessage(Message message) {
        message.setSourceId(associatedConnectionId);
        return message;
    }
}
