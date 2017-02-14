package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of this class are used for message parsing from
 */
public abstract class AbstractMessageParser {
    private ByteBuffer parseBuffer;
    private LinkedBlockingQueue<Message> readyMessages;

    protected AbstractMessageParser(LinkedBlockingQueue<Message> readyMessages) {
        parseBuffer = ByteBuffer.allocate(20000);
        this.readyMessages = readyMessages;
    }

    protected final void appendDataForParsing(ByteBuffer dataToAppend) throws ParseException {

        parseBuffer.put(dataToAppend);

        do {

            Message nextMessage = Message.parse(parseBuffer);

            if (nextMessage != null) {

                readyMessages.offer(prepareNewMessage(nextMessage));

            } else {
                break;
            }

        } while (true);

    }

    protected abstract Message prepareNewMessage(Message message);

    //TODO: make external method to add message to message queue

    private void expandBuffer(int appendSize) {
        parseBuffer.flip();
        ByteBuffer newBuffer = ByteBuffer.allocate(parseBuffer.capacity() + appendSize);
        newBuffer.put(parseBuffer);
        parseBuffer = newBuffer;
    }

    private void trimBuffer() {
        parseBuffer.flip();
        int trimTo = parseBuffer.remaining();
        ByteBuffer newBuffer = ByteBuffer.allocate(trimTo);
        newBuffer.put(parseBuffer);
        parseBuffer = newBuffer;
    }

}
