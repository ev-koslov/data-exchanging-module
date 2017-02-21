package ev.koslov.data_exchanging.module;


import ev.koslov.data_exchanging.components.Message;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of this class are used for message parsing from
 */
abstract class AbstractMessageParser {
    private ByteBuffer parseBuffer;
    private final LinkedBlockingQueue<Message> readyMessages;

    AbstractMessageParser(LinkedBlockingQueue<Message> readyMessages) {
        parseBuffer = ByteBuffer.allocate(0);
        this.readyMessages = readyMessages;
    }

    final void appendDataForParsing(ByteBuffer dataToAppend) throws ParseException {
        if (parseBuffer.remaining()<dataToAppend.remaining()){
            expandBuffer(dataToAppend.remaining() - parseBuffer.remaining());
        }

        parseBuffer.put(dataToAppend);

        do {

            Message nextMessage = Message.parse(parseBuffer);

            if (nextMessage != null) {

                readyMessages.offer(prepareNewMessage(nextMessage));

            } else {
                break;
            }

        } while (true);

        trimBuffer();

    }

    abstract Message prepareNewMessage(Message message);

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
