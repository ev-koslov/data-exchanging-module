package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;
import java.util.concurrent.*;

/**
 * Main abstract class of DataExchanging module. Implementations are used to communicate with each other using SocketChannel.
 */
public abstract class AbstractEndpoint {
    private final LinkedBlockingQueue<Message> readyMessages;
    private final ExecutorService executorService;

    /**
     * Creates instance of endpoint using given {@link AbstractEndpointInterface} implementation.
     * @param endpointInterface implementation of {@link AbstractEndpointInterface} which is used to communicate with
     *                          other side of endpoint (remote side in network).
     */
    protected AbstractEndpoint(AbstractEndpointInterface endpointInterface) {
        readyMessages = new LinkedBlockingQueue<Message>();
        executorService = Executors.newCachedThreadPool();

        //make current endpoint associated with given endpoint interface
        endpointInterface.setEndpoint(this);

        MessageSorter messageSorter = new MessageSorter(this, endpointInterface);

        executeTask(messageSorter);
    }

    protected final Future executeTask(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException();
        }
        return executorService.submit(runnable);
    }

    protected final <V> Future<V> executeTask(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        return executorService.submit(callable);
    }

    protected final LinkedBlockingQueue<Message> getReadyMessages() {
        return readyMessages;
    }

    protected Message getNextReadyMessage() throws InterruptedException {
        return readyMessages.take();
    }

    protected void shutdown() {
        executorService.shutdownNow();
        readyMessages.clear();
    }

    protected boolean isRunning() {
        return !this.executorService.isShutdown();
    }

    //TODO: блокирующий метод, возвращает исключение, по которому остановился ендпоинт. Ждет остановки ендпоинта.
//    public final void join() throws Exception{
//
//    }
}
