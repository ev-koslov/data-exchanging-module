package ev.koslov.data_exchanging.client;

import ev.koslov.data_exchanging.common.AbstractMessageParser;
import ev.koslov.data_exchanging.components.Message;

import java.util.concurrent.LinkedBlockingQueue;


final class ClientMessageParser extends AbstractMessageParser {

    protected ClientMessageParser(LinkedBlockingQueue<Message> readyMessages) {
        super(readyMessages);
    }

    @Override
    protected Message prepareNewMessage(Message message) {
        return message;
    }

}
