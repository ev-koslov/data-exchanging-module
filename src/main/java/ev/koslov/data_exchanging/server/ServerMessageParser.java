package ev.koslov.data_exchanging.server;

import ev.koslov.data_exchanging.common.AbstractMessageParser;
import ev.koslov.data_exchanging.components.Message;

import java.util.concurrent.LinkedBlockingQueue;


final class ServerMessageParser extends AbstractMessageParser {
    private final long associatedConnectionId;

    ServerMessageParser(LinkedBlockingQueue<Message> readyMessages, long associatedConnectionId) {
        super(readyMessages);
        this.associatedConnectionId = associatedConnectionId;
    }

    @Override
    protected Message prepareNewMessage(Message message) {
        message.getHeader().setSourceId(associatedConnectionId);
        return message;
    }
}
