package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.server.ServerConnection;
import ev.koslov.data_exchanging.server.ServerEndpoint;


public abstract class ServerInterface extends AbstractEndpointInterface<ServerEndpoint<?>> {


    @Override
    final boolean isResponse(Message responseMessage) {
        return responseMessage.getMessageType().equals(MessageTypeTag.CLIENT_TO_SERVER_RESPONSE);
    }

    @Override
    final void processRequest(Message requestMessage) {
        switch (requestMessage.getMessageType()) {
            case CLIENT_TO_SERVER_REQUEST: {
                processRequestFromClient(requestMessage);
                break;
            }
            case CLIENT_TO_CLIENT_REQUEST: {
                processRequestFromClientToClient(requestMessage);
                break;
            }
            case CLIENT_TO_CLIENT_RESPONSE: {
                processResponseFromClientToClient(requestMessage);
                break;
            }
            default: {
                throw new UnsupportedOperationException("This packet type can't be processed on server side: " + requestMessage.getMessageType());
            }
        }
    }

    @Override
    public final void send(Message messageToSend) {
        ServerConnection connection = getEndpoint().getConnection(messageToSend.getTargetId());
        if (connection != null) {
            connection.sendMessage(messageToSend);
        }
    }


    protected abstract void processRequestFromClient(Message request);

    protected abstract void processRequestFromClientToClient(Message request);

    protected abstract void processResponseFromClientToClient(Message request);
}
