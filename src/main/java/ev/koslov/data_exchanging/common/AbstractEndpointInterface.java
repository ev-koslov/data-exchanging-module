package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.MessageFabric;
import ev.koslov.data_exchanging.components.Request;
import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.exceptions.DeniedException;
import ev.koslov.data_exchanging.exceptions.InvalidTagException;
import ev.koslov.data_exchanging.exceptions.RequestException;
import ev.koslov.data_exchanging.exceptions.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class, which implementations are used to make communication beetwen client and server easier.
 *
 * @param <C> implementation of {@link AbstractConnection}
 * @param <E> implementation of {@link AbstractEndpoint}
 */
public abstract class AbstractEndpointInterface<E extends AbstractEndpoint<?>> {
    private E endpoint;
    private Map<Long, Request> requestsMap;
    private MessageFabric messageFabric;
    private boolean readyForProcessing;

    protected AbstractEndpointInterface() {
        this.requestsMap = new HashMap<Long, Request>();
        this.messageFabric = new MessageFabric();
    }

    /**
     * This method used to set implementation of {@link AbstractEndpoint}, associated with this interface.
     *
     * @param endpoint endpoint to associate with.
     */
    final void setEndpoint(E endpoint) {
        this.endpoint = endpoint;
        this.readyForProcessing = true;
    }

    /**
     * Iterates requestsMap and notifies any Thread that wait for response that will be no responses anymore. Any thread
     * which notified by this method, will receive {@link TimeoutException} because of receiving no response.
     */
    final void cancelRequests() {
        readyForProcessing = false;
        List<Request> requests = new ArrayList<Request>(requestsMap.values());
        for (Request request : requests) {
            synchronized (request) {
                request.notifyAll();
            }
        }
        requestsMap.clear();
    }

     /**
     * Registers message as request for remote side. Only theese message tags ({@link Message#getMessageType()}) are allowed:
     * <li>
     *     <ul>{@link ev.koslov.data_exchanging.components.tags.MessageTypeTag#CLIENT_TO_CLIENT_REQUEST}</ul>
     *     <ul>{@link ev.koslov.data_exchanging.components.tags.MessageTypeTag#CLIENT_TO_SERVER_REQUEST}</ul>
     *     <ul>{@link ev.koslov.data_exchanging.components.tags.MessageTypeTag#SERVER_TO_CLIENT_REQUEST}</ul>
     * </li>
      * <br> Otherwise {@link InvalidTagException} will be thrown.
     * @param requestMessage message to register.
     * @throws InvalidTagException if message has not allowed tag.
     *
     */
    private void registerRequest(Message requestMessage) throws InvalidTagException {
        if (!requestMessage.getMessageType().name().endsWith("REQUEST")) {
            throw new InvalidTagException("Wrong message type tag: "+requestMessage.getMessageType()+". Only request tags are allowed.");
        }
        Request request = new Request(requestMessage);
        requestsMap.put(request.getRequestId(), request);
    }

    //TODO: Подумать, как сделать методы registerRequest, waitResponse переопределяемыми в подклассах. Например, на сервере
    //TODO: если приходит запрос от клиента к другому клиенту, создаем обьект запроса на сервере на основе сообщение,
    //TODO: и блокируем орабатывающий поток до прихода ответа от клиента на сервер (tag CLIENT_TO_CLIENT_RESPONSE),
    //TODO: тогда вызываем processResponse на сервере.

    //TODO: Переопределять ничего не надо. На основе запроса создаем еще один запрос на сервере, только уже в сторону клиента
    //TODO: После прихода ответа от клиента на сервер, подменяем responseForRequestId на запомненный ранее и отправляем запросившему клиенту
    //TODO: Если случается ошибка (таймаут и т.п.), то заполнаяем в сообещнии стату и отправляем клиенту.

    /**
     * Blocking method that wait for response is being processed on the remote side.
     * @param requestMessage request message
     * @param timeout operation timeout. When time is up, {@link TimeoutException} will be thrown.
     * @return response message.
     * @throws InterruptedException
     * @throws RequestException if no message had been received or received message with not {@link ev.koslov.data_exchanging.components.tags.StatusTag#OK} tag.
     */
    private Message waitResponse(Message requestMessage, long timeout) throws InterruptedException, RequestException {

        //getting request, associated with request message
        Request request = requestsMap.get(requestMessage.getRequestId());

        //blocking invoked thread for timeout
        synchronized (request) {
            request.wait(timeout);
        }

        //after time is out or notification, removing associated request from requestsMap.
        requestsMap.remove(request.getRequestId());



        if (request.getResponseMessage() != null) {

            //getting response message and checking its status
            Message message = request.getResponseMessage();

            //TODO: add response message status processing
            switch (message.getStatus()) {
                case OK: {
                    return message;
                }
                case DENIED: {
                    throw new DeniedException();
                }
                case TIMEOUT: {
                    throw new TimeoutException();
                }
                default: {
                    throw new RuntimeException("INVALID STATUS CODE");
                }
            }

        } else {
            //if request has no response message, throwing TimeoutException
            throw new TimeoutException();
        }
    }

    /**
     * Invocation of this method will associate received message with request and notify waiting thread that response is received
     * If message has no associated request, message will be discarded.
     * @param responseMessage message to associate with request
     */
    final void processResponse(Message responseMessage) {
        if (requestsMap.containsKey(responseMessage.getResponseForRequestId())) {

            Request request = requestsMap.get(responseMessage.getResponseForRequestId());

            if (request != null) {
                request.setResponseMessage(responseMessage);

                synchronized (request) {
                    request.notifyAll();
                }
            }

        }
    }

    /**
     * Tests if message if response and can be processed in invoked implementations
     * @param responseMessage message to test
     * @return true if message can be processed as response on "this" side
     */
    abstract boolean isResponse(Message responseMessage);

    /**
     * Processes request from the remote side.
     * @param requestMessage message to process
     */
    abstract void processRequest(Message requestMessage);

    public final E getEndpoint() {
        return endpoint;
    }

    /**
     * sends message to associated connection
     * @param messageToSend message to append to outboxing queue
     */
    public abstract void send(Message messageToSend);

    /**
     * Blocking method. Sequentially invokes theese methods {@link AbstractEndpointInterface#registerRequest(Message)},
     * {@link AbstractEndpointInterface#send(Message)} and {@link AbstractEndpointInterface#waitResponse(Message, long)}
     * @param message message to append to outboxing queue
     * @param timeout operation timeout.
     * @return result of message processing
     * @throws InterruptedException
     * @throws RequestException if no message had been received or received message with not {@link ev.koslov.data_exchanging.components.tags.StatusTag#OK} tag.
     */
    public final Message request(Message message, long timeout) throws InterruptedException, RequestException {
        registerRequest(message);
        send(message);
        return waitResponse(message, timeout);
    }

    public final MessageFabric getMessageFabric() {
        return messageFabric;
    }
}
