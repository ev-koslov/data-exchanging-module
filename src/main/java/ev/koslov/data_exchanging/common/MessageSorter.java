package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;

public final class MessageSorter implements Runnable {

    private final AbstractEndpoint endpoint;
    private final AbstractEndpointInterface endpointInterface;

    public MessageSorter(AbstractEndpoint endpoint, AbstractEndpointInterface endpointInterface) {
        this.endpoint = endpoint;
        this.endpointInterface = endpointInterface;
    }

    public final void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {

                    //getting next parsed message from endpoint
                    final Message message = endpoint.getNextReadyMessage();

                    if (endpointInterface.isResponse(message)){
                        endpointInterface.processResponse(message);
                    } else {
                        endpointInterface.processRequest(message);
                    }

                    //TODO: perform processing of each message in separate thread. Maybe it is not a good idea?
//                    endpoint.getExecutorService().execute(new Runnable() {
//                        public void run() {
//                            if (endpointInterface.isResponse(message)){
//                                endpointInterface.processResponse(message);
//                            } else {
//                                endpointInterface.processRequest(message);
//                            }
//                        }
//                    });

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
