package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.client.ClientConnection;
import ev.koslov.data_exchanging.client.ClientEndpoint;
import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.tags.MessageTypeTag;

public abstract class ClientInterface<C extends ClientConnection> extends AbstractEndpointInterface<C, ClientEndpoint<C, ?>> {


    @Override
    final boolean isResponse(Message responseMessage) {
        MessageTypeTag messageTypeTag = responseMessage.getMessageType();

        return messageTypeTag.equals(MessageTypeTag.CLIENT_TO_CLIENT_RESPONSE) ||
                messageTypeTag.equals(MessageTypeTag.SERVER_TO_CLIENT_RESPONSE);
    }

    @Override
    final void processRequest(Message requestMessage) {
        switch (requestMessage.getMessageType()) {
            case SERVER_TO_CLIENT_REQUEST: {
                processRequestFromServer(requestMessage);
                break;
            }
            case CLIENT_TO_CLIENT_REQUEST: {
                processRequestFromClient(requestMessage);
                break;
            }
            default: {
                throw new UnsupportedOperationException("This packet type can't be processed on client side: " + requestMessage.getMessageType());
            }
        }
    }

    @Override
    public final void send(Message messageToSend) {
        getEndpoint().getConnection().sendMessage(messageToSend);
    }

    protected abstract void processRequestFromServer(Message request);

    protected abstract void processRequestFromClient(Message request);
}
