package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Implementations of this subclass are holding message parsing mechanism ({@link AbstractMessageParser}) and list that contains
 * byteBuffers of messages which needs to be sent using {@link AbstractDataExchanger}.
 * Implementations SHOULD NOT BE instantiated in separate of {@link AbstractEndpoint} implementations.
 */
public abstract class AbstractConnection {
    private SocketChannel channel;
    private SelectionKey selectionKey;
    private AbstractMessageParser messageParser;

    private Properties properties;
    private Object attachment;


    //messages to send to the socketChannel
    private final LinkedList<ByteBuffer> outboxingMessages;

    /**
     * Creates server connection instance.
     *
     * @param selectionKey  selection key for newly created socketChannel
     * @param messageParser associated message parser
     */
    protected AbstractConnection(SelectionKey selectionKey, AbstractMessageParser messageParser) {
        if (selectionKey == null || messageParser == null) {
            throw new NullPointerException();
        }

        if (!selectionKey.isValid()) {
            throw new IllegalArgumentException("Given selection key is invalid.");
        }

        this.outboxingMessages = new LinkedList<ByteBuffer>();
        this.properties = new Properties();

        //getting socket channel associated with given selector
        this.channel = (SocketChannel) selectionKey.channel();
        this.selectionKey = selectionKey;
        this.messageParser = messageParser;

        //attaching connection instance to the selectionKey
        selectionKey.attach(this);

        //make selection key interested in reading
        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    /**
     * Delegated method {@link AbstractMessageParser#appendDataForParsing(ByteBuffer)}
     *
     * @param dataToAppend data to append to parsing queue
     * @throws ParseException if expected and actual data are not match
     */
    final void appendDataForParsing(ByteBuffer dataToAppend) throws ParseException {
        messageParser.appendDataForParsing(dataToAppend);
    }

    /**
     * Adds message object to output queue. Invoking this method does not guarantee that message will be sent immediately
     * Message will be sent as soon as possible o will be discarded if some IOException occurs (closed socket, etc.)
     *
     * @param message message to send
     */
    //TODO: add blocking SendMessage
    protected final void sendMessage(Message message) {
        if (selectionKey.isValid()) {

            //TODO: how to set limited size of outboxing message queue
//            if (outboxingMessages.size() > 1000){
//                synchronized (outboxingMessages){
//                    try {
//                        outboxingMessages.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

            synchronized (outboxingMessages) {
                //add message data to output queue (wrap bytes by ByteBuffer)
                outboxingMessages.addLast(message.getAsReadReadyByteBuffer());
            }

            //make selection key interested in write
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        }
    }

    /**
     * Tells if outboxing messageQueue has some more data to send
     * and removes already sent data (empty byteBuffers).
     * To make data correct should be invoked every time before {@link AbstractConnection#getNextOutboxingMessage()}
     *
     * @return true if and only if outboxingMessage list contains at least one non empty byteBuffer
     */
    final boolean hasOutboxingMessages() {
        synchronized (outboxingMessages) {
            //if outboxingMessage list is not empty and first element has no more data to send, removing it
            if (!outboxingMessages.isEmpty() && !outboxingMessages.getFirst().hasRemaining()) {
                outboxingMessages.removeFirst().clear();
            }

            //TODO: wake up any random object that wants to append data to send queue
//            if (outboxingMessages.size()<1000){
//                synchronized (outboxingMessages){
//                    outboxingMessages.notify();
//                }
//            }

            //if outboxingMessage is empty setting selector interested in reading only
            if (outboxingMessages.isEmpty()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }

            return !outboxingMessages.isEmpty();
        }
    }

    /**
     * Get next message to send
     * User must invoke {@link AbstractConnection#hasOutboxingMessages()} method first and ensure that method returned True.
     * Otherwise {@link NullPointerException} may be thrown.
     *
     * @return {@link ByteBuffer} containing prepared message bytes.
     */
    final ByteBuffer getNextOutboxingMessage() {
        synchronized (outboxingMessages) {
            return outboxingMessages.getFirst();
        }
    }

    /**
     * Close connection and release any resources bound to it
     */
    protected void close() {

        selectionKey.cancel();

        try {
            channel.close();
        } catch (IOException e) {
            //do nothing
        }

        outboxingMessages.clear();

    }

    /**
     * Returns a connection state
     *
     * @return true is assigned selection key is valid and channel is open, false otherwise
     */
    public boolean isConnected() {
        return selectionKey.isValid() && channel.isOpen();
    }

    public Properties getProperties() {
        return properties;
    }

    public void attach(Object attachment) {
        this.attachment = attachment;
    }

    public Object attachment() {
        return attachment;
    }
}
