package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.client.Client;
import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestMessageBody;
import ev.koslov.data_exchanging.components.ResponseMessageBody;
import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.exceptions.RequestException;

import java.io.IOException;

public abstract class AbstractClientInterface extends AbstractEndpointInterface<Client> {

    public void connect(String host, int port) throws IOException {
        if (super.getAssociatedEndpoint() != null && super.getAssociatedEndpoint().isRunning()){
            throw new UnsupportedOperationException("Client is already running.");
        }

        //just creating client instance. It will be associated with current interface in constructor
        new Client(host, port, this);
    }

    public boolean isConnected() {
        return (super.getAssociatedEndpoint() != null) && super.getAssociatedEndpoint().isRunning();
    }

    public void disconnect() {
        Client client = super.getAssociatedEndpoint();
        if (client != null) {
            client.shutdown();
        }
    }

    @Override
    final boolean isResponse(Message responseMessage) {
        MessageTypeTag messageTypeTag = responseMessage.getHeader().getMessageType();

        return messageTypeTag.equals(MessageTypeTag.CLIENT_TO_CLIENT_RESPONSE) ||
                messageTypeTag.equals(MessageTypeTag.SERVER_TO_CLIENT_RESPONSE);
    }

    @Override
    final void processRequest(Message requestMessage) {
        switch (requestMessage.getHeader().getMessageType()) {
            case SERVER_TO_CLIENT_REQUEST: {
                processRequestFromServer(requestMessage);
                break;
            }
            case CLIENT_TO_CLIENT_REQUEST: {
                processRequestFromClient(requestMessage);
                break;
            }
            default: {
                throw new UnsupportedOperationException("This packet type can't be processed on client side: " + requestMessage.getHeader().getMessageType());
            }
        }
    }

    @Override
    final void send(Message messageToSend) {
        getAssociatedEndpoint().getConnection().sendMessage(messageToSend);
    }

    public void clientToServerRequest(RequestMessageBody requestMessageBody) {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.CLIENT_TO_SERVER_REQUEST);
        message.setBody(requestMessageBody);
        send(message);
    }

    public void clientToClientRequest(long targetId, RequestMessageBody requestMessageBody) {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.CLIENT_TO_CLIENT_REQUEST);
        message.getHeader().setTargetId(targetId);
        message.setBody(requestMessageBody);
        send(message);
    }

    public ResponseMessageBody clientToServerRequest(RequestMessageBody requestMessageBody, long timeout) throws RequestException, InterruptedException {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.CLIENT_TO_SERVER_REQUEST);
        message.setBody(requestMessageBody);
        return request(message, timeout);
    }

    public ResponseMessageBody clientToClientRequest(long targetId, RequestMessageBody requestMessageBody, long timeout) throws InterruptedException, RequestException {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.CLIENT_TO_CLIENT_REQUEST);
        message.getHeader().setTargetId(targetId);
        message.setBody(requestMessageBody);
        return request(message, timeout);
    }

    /**
     * Processes requests from server to client
     * @param request request to process
     */
    protected abstract void processRequestFromServer(Message request);

    /**
     * Processes requests from remote client to this client
     * @param request request to process
     */
    protected abstract void processRequestFromClient(Message request);

}
