package ev.koslov.data_exchanging.common;


import ev.koslov.data_exchanging.client.Client;
import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestMessageBody;
import ev.koslov.data_exchanging.components.ResponseMessageBody;
import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.exceptions.RequestException;
import ev.koslov.data_exchanging.server.Server;
import ev.koslov.data_exchanging.server.ServerConnection;

import java.io.IOException;


public abstract class AbstractServerInterface extends AbstractEndpointInterface<Server> {

    public void startServer(int port) throws IOException {
        if (super.getEndpoint() != null && super.getEndpoint().isRunning()){
            throw new UnsupportedOperationException("Server is already running.");
        }

        //just creating server instance. It will be associated with current interface in constructor
        new Server(port, this);
    }

    public boolean isServerRunning() {
        Server server = getEndpoint();
        return server != null && server.isRunning();
    }

    public void stopServer() {
        Server server = getEndpoint();
        if (server != null && server.isRunning()){
            server.shutdown();
        }
    }

    @Override
    final boolean isResponse(Message responseMessage) {
        return responseMessage.getHeader().getMessageType().equals(MessageTypeTag.CLIENT_TO_SERVER_RESPONSE);
    }

    @Override
    final void processRequest(Message requestMessage) {
        switch (requestMessage.getHeader().getMessageType()) {
            case CLIENT_TO_SERVER_REQUEST: {
                processRequestFromClient(requestMessage);
                break;
            }
            case CLIENT_TO_CLIENT_REQUEST:
            case CLIENT_TO_CLIENT_RESPONSE: {
                processMessageFromClientToClient(requestMessage);
                break;
            }
            default: {
                throw new UnsupportedOperationException("This packet type can't be processed on server side: " + requestMessage.getHeader().getMessageType());
            }
        }
    }

    @Override
    final void send(Message messageToSend) {
        ServerConnection connection = getEndpoint().getConnection(messageToSend.getHeader().getTargetId());
        if (connection != null) {
            connection.sendMessage(messageToSend);
        }
    }

    public void serverToClientRequest(long targetId, RequestMessageBody requestMessageBody) {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.SERVER_TO_CLIENT_REQUEST);
        message.getHeader().setTargetId(targetId);
        message.setBody(requestMessageBody);
        send(message);
    }

    public ResponseMessageBody serverToClientRequest(long targetId, RequestMessageBody requestMessageBody, long timeout) throws InterruptedException, RequestException {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.SERVER_TO_CLIENT_REQUEST);
        message.getHeader().setTargetId(targetId);
        message.setBody(requestMessageBody);
        return request(message, timeout);
    }

    /**
     * Processes requests from client to server
     * @param request request to process
     */
    protected abstract void processRequestFromClient(Message request);

    /**
     * Processes messages from client to another client (after processing message should be forwarded to client)
     * @param request message to process
     */
    protected abstract void processMessageFromClientToClient(Message request);

}
