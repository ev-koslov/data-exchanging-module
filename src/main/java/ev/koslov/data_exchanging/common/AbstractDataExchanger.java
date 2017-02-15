package ev.koslov.data_exchanging.common;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Implementation of this class are performing data exchanging using non-blocking {@link SocketChannel} which is bound to
 * embedded selector.
 * @param <T> implementation of {@link AbstractConnection} which is used to serve data exchanging by providing {@link AbstractMessageParser}
 *           implementations for received data parsing and outboxing messages queue for data sending.
 */
public abstract class AbstractDataExchanger<T extends AbstractConnection> implements Runnable{

    private Selector selector;
    private ByteBuffer readBuffer;

    protected AbstractDataExchanger() throws IOException {
        selector = Selector.open();
        readBuffer = ByteBuffer.allocate(1024);
    }

    /**
     * Performs channel registration to embedded selector for future data exchanging using given {@link SocketChannel}.
     * @param channel channel to register with selector.
     *                <br></R>ATTENTION: given socketChannel MUST NOT be registered with any selector
     *                or {@link UnsupportedOperationException} will be thrown.
     * @return {@link SelectionKey} which is bound with given socketChannel. <br> NOTE: returned {@link SelectionKey} is
     * not interested in any operations ({@link SelectionKey#interestOps()} == 0). Interest ops should be set later using
     * {@link SelectionKey#interestOps(int)}.
     * @throws IOException if some IO exceptions are occurred during registration.
     */
    protected SelectionKey registerChannel(SocketChannel channel) throws IOException {
        if (channel.isRegistered()) {
            throw new UnsupportedOperationException("Given channel must not be registered.");
        }

        //switch channel to non-blocking mode
        channel.configureBlocking(false);

        //register channel with internal selector and return selectionKey
        return channel.register(selector, 0);
    }

    /**
     * This {@link Runnable} implementation is used to perform loop operations in separate {@link Thread}. It performs
     * loop selection of all interested keys, that are ready for interested operations.
     */
    public final void run() {

        try {

            while (!Thread.currentThread().isInterrupted()) {

                if (selector.select(10) > 0) {

                    Iterator<SelectionKey> selectedKeysIterator = selector.selectedKeys().iterator();

                    while (selectedKeysIterator.hasNext()) {

                        //get selection key and remove it from iterator
                        SelectionKey key = selectedKeysIterator.next();
                        selectedKeysIterator.remove();

                        //if selection key is invalid, skip it
                        if (!key.isValid()) {
                            continue;
                        }

                        //get connection implementation instance from key
                        @SuppressWarnings("unchecked")
                        T connection = (T) key.attachment();

                        //getting selected channel from a selection key
                        SocketChannel selectedChannel = (SocketChannel) key.channel();

                        try {

                            //Reading from selected channel
                            if (key.isReadable()) {
                                //Reading data from socketChannel
                                readBuffer.clear();

                                if (selectedChannel.read(readBuffer) == -1){
                                    throw new IOException("Channel was closed from remote side.");
                                }

                                readBuffer.flip();

                                //Write RAW data to AbstractConnection object
                                connection.appendDataForParsing(readBuffer);

                            }

                            //Writing data to selected channel if exist
                            if (connection.isConnected() && connection.hasOutboxingMessages() && key.isWritable()) {
                                selectedChannel.write(connection.getNextOutboxingMessage());
                            }

                        } catch (Exception e) {

                            //Exception from current serverConnection
                            e.printStackTrace();

                            closeConnection(connection);

                        }

                    }
                }
            }

        } catch (IOException e) {
            //Exception from selector
            e.printStackTrace();
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                //do nothing
            }
        }
    }

    /**
     * Closes given connection
     * @param connection connection to close
     */
    protected abstract void closeConnection(T connection);
}
