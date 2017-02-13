package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;

import java.lang.reflect.Constructor;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main abstract class of DataExchanging module. Implementations are used to communicate with each other using SocketChannel.
 * @param <I> implementation of {@link AbstractEndpointInterface}
 */
public abstract class AbstractEndpoint<I extends AbstractEndpointInterface> {
    private final LinkedBlockingQueue<Message> readyMessages;
    private final ExecutorService executorService;

    private final MessageSorter messageSorter;
    private final I endpointInterface;

    /**
     * Creates instance of endpoint using given {@link AbstractEndpointInterface} implementation.
     * @param endpointInterface implementation of {@link AbstractEndpointInterface} which is used to communicate with
     *                          other side of endpoint (remote side in network).
     */
    protected AbstractEndpoint(I endpointInterface) {
        readyMessages = new LinkedBlockingQueue<Message>();
        executorService = Executors.newCachedThreadPool();

        this.endpointInterface = endpointInterface;

        //make current endpoint associated with given endpoint interface
        this.endpointInterface.setEndpoint(this);

        this.messageSorter = new MessageSorter(this, endpointInterface);
    }


    protected final LinkedBlockingQueue<Message> getReadyMessages() {
        return readyMessages;
    }

    protected final ExecutorService getExecutorService() {
        return executorService;
    }

    protected final MessageSorter getMessageSorter() {
        return messageSorter;
    }

    protected Message getNextReadyMessage() throws InterruptedException {
        return readyMessages.take();
    }

    protected void shutdown() {
        executorService.shutdownNow();
        readyMessages.clear();
    }

    /**
     * Get an associated {@link AbstractEndpointInterface} implementation.
     * @return bound {@link AbstractEndpointInterface} implementation
     */
    public final I getInterface(){
        return endpointInterface;
    }

    public boolean isRunning() {
        return !this.executorService.isShutdown();
    }

    //TODO: блокирующий метод, возвращает исключение, по которому остановился ендпоинт. Ждет остановки ендпоинта.
//    public final void join() throws Exception{
//
//    }
}
