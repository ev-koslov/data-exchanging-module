package ev.koslov.data_exchanging.module;

import ev.koslov.data_exchanging.components.Message;

import java.util.concurrent.LinkedBlockingQueue;


final class ClientMessageParser extends AbstractMessageParser {

    ClientMessageParser(LinkedBlockingQueue<Message> readyMessages) {
        super(readyMessages);
    }

    @Override
    Message prepareNewMessage(Message message) {
        return message;
    }

}
