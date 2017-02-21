package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.ResponseBody;
import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.components.tags.StatusTag;
import ev.koslov.data_exchanging.exceptions.DeniedException;
import ev.koslov.data_exchanging.exceptions.InvalidTagException;
import ev.koslov.data_exchanging.exceptions.RequestException;
import ev.koslov.data_exchanging.exceptions.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class, which implementations are used to make communication between client and server easier.
 *
 * @param <E> implementation of {@link AbstractEndpoint}
 */
abstract class AbstractEndpointInterface<E extends AbstractEndpoint> {
    private E associatedEndpoint;
    private final Map<Long, Request> requestsMap;

    AbstractEndpointInterface() {
        this.requestsMap = new HashMap<Long, Request>();
    }

    /**
     * This method used to set implementation of {@link AbstractEndpoint}, associated with this interface.
     *
     * @param associatedEndpoint endpoint to associate with.
     */
    final void setAssociatedEndpoint(E associatedEndpoint) {
        this.associatedEndpoint = associatedEndpoint;
    }

    public E getAssociatedEndpoint() {
        return associatedEndpoint;
    }

    /**
     * Iterates requestsMap and notifies any Thread that wait for response that will be no responses anymore. Any thread
     * which notified by this method, will receive {@link TimeoutException} because of receiving no response.
     */
    final void cancelRequests() {
        List<Request> requests = new ArrayList<Request>(requestsMap.values());
        for (Request request : requests) {
            synchronized (request) {
                request.notifyAll();
            }
        }
        requestsMap.clear();
    }

     /**
     * Registers message as request for remote side. Only these message tags ({@link Message.Header#getMessageType()}) are allowed:
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
        if (!requestMessage.getHeader().getMessageType().name().endsWith("REQUEST")) {
            throw new InvalidTagException("Wrong message type tag: "+requestMessage.getHeader().getMessageType()+". Only request tags are allowed.");
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
        Request request = requestsMap.get(requestMessage.getHeader().getRequestId());

        //blocking invoked thread for timeout
        synchronized (request) {
            request.wait(timeout);
        }

        //after time is out or notification, removing associated request from requestsMap.
        requestsMap.remove(request.getRequestId());



        if (request.getResponseMessage() != null) {

            //getting response message and checking its status
            Message responseMessage = request.getResponseMessage();

            //TODO: add response message status processing
            switch (responseMessage.getHeader().getStatus()) {
                case OK: {
                    return responseMessage;
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
        Message.Header header = responseMessage.getHeader();

        if (requestsMap.containsKey(header.getResponseForRequestId())) {

            Request request = requestsMap.get(header.getResponseForRequestId());

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


    /**
     * sends message to associated connection
     * @param messageToSend message to append to outboxing queue
     */
    abstract void send(Message messageToSend) throws IOException;


    //NEW METHODS!!!!!!!

    /**
     * Blocking method. Sequentially invokes these methods {@link AbstractEndpointInterface#registerRequest(Message)},
     * {@link AbstractEndpointInterface#send(Message)} and {@link AbstractEndpointInterface#waitResponse(Message, long)}
     * @param message message to append to outboxing queue
     * @param timeout operation timeout.
     * @return result of message processing represented by ResponseBody
     * @throws InterruptedException
     * @throws RequestException if no message had been received or received message with not {@link ev.koslov.data_exchanging.components.tags.StatusTag#OK} tag.
     */
    final ResponseBody request(Message message, long timeout) throws InterruptedException, IOException {
        registerRequest(message);
        send(message);
        Message responseBody = waitResponse(message, timeout);

        return responseBody.getBody();
    }

    public final void response(Message.Header requestHeader, StatusTag statusTag, ResponseBody messageBody) throws IOException {
        Message responseMessage = new Message();
        Message.Header responseHeader = responseMessage.getHeader();
        switch (requestHeader.getMessageType()) {
            case SERVER_TO_CLIENT_REQUEST: {
                responseHeader.setMessageType(MessageTypeTag.CLIENT_TO_SERVER_RESPONSE);
                break;
            }
            case CLIENT_TO_SERVER_REQUEST: {
                responseHeader.setMessageType(MessageTypeTag.SERVER_TO_CLIENT_RESPONSE);
                break;
            }
            case CLIENT_TO_CLIENT_REQUEST: {
                responseHeader.setMessageType(MessageTypeTag.CLIENT_TO_CLIENT_RESPONSE);
                break;
            }
        }

        responseHeader.setTargetId(requestHeader.getSourceId());
        responseHeader.setStatus(statusTag);
        responseHeader.setResponseForRequestId(requestHeader.getRequestId());

        responseMessage.setBody(messageBody);

        send(responseMessage);
    }

    private class Request {
        private final long requestId;
        private Message responseMessage;

        public Request(Message requestMessage) {
            this.requestId = System.nanoTime();
            requestMessage.getHeader().setRequestId(requestId);
            //TODO: set request ID to message
        }

        public long getRequestId() {
            return requestId;
        }

        public Message getResponseMessage() {
            return responseMessage;
        }

        public void setResponseMessage(Message responseMessage) {
            if (responseMessage.getHeader().getResponseForRequestId() == requestId) {
                this.responseMessage = responseMessage;
            } else {
                throw new IllegalArgumentException("Response message is not for this request: " + responseMessage.getHeader().getResponseForRequestId() + "!=" + requestId);
            }
        }
    }
}
