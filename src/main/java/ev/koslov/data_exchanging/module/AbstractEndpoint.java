package ev.koslov.data_exchanging.module;


import ev.koslov.data_exchanging.components.Message;

import java.util.concurrent.*;

/**
 * Main abstract class of DataExchanging module. Implementations are used to communicate with each other using SocketChannel.
 */
abstract class AbstractEndpoint {
    private final LinkedBlockingQueue<Message> readyMessages;
    private final ExecutorService executorService;
    private final AbstractEndpointInterface endpointInterface;

    /**
     * Creates instance of endpoint using given {@link AbstractEndpointInterface} implementation.
     *
     * @param endpointInterface implementation of {@link AbstractEndpointInterface} which is used to communicate with
     *                          other side of endpoint (remote side in network).
     */
    AbstractEndpoint(AbstractEndpointInterface endpointInterface) {
        this.readyMessages = new LinkedBlockingQueue<Message>();
        this.executorService = Executors.newCachedThreadPool();
        this.endpointInterface = endpointInterface;

        //make current endpoint associated with given endpoint interface
        endpointInterface.associateEndpoint(this);

        executeTask(new MessageSorterWorker());
    }

    final LinkedBlockingQueue<Message> getReadyMessages() {
        return readyMessages;
    }

    Message getNextReadyMessage() throws InterruptedException {
        return readyMessages.take();
    }

    public void shutdown() {
        executorService.shutdownNow();
        readyMessages.clear();
        //destroying association between endpoint and endpoint interface
        endpointInterface.associateEndpoint(null);
    }

    public final Future executeTask(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException();
        }
        return executorService.submit(runnable);
    }

    public final <V> Future<V> executeTask(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        return executorService.submit(callable);
    }

    public boolean isRunning() {
        return !this.executorService.isShutdown();
    }

    //TODO: блокирующий метод, возвращает исключение, по которому остановился ендпоинт. Ждет остановки ендпоинта.
//    public final void join() throws Exception{
//
//    }


    //TODO: Endpoint embedded workers

    private class MessageSorterWorker implements Runnable {

        public final void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {

                        //getting next parsed message from endpoint
                        final Message message = AbstractEndpoint.this.getNextReadyMessage();

                        //TODO: perform processing of each message in separate thread. Maybe it is not a good idea?

                        executeTask(new Runnable() {
                            public void run() {
                                if (endpointInterface.isResponse(message)) {
                                    endpointInterface.processResponse(message);
                                } else {
                                    endpointInterface.processRequest(message);
                                }
                            }
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                endpointInterface.cancelRequests();
                //TODO: shutdown endpoint instance
            }
        }
    }
}
