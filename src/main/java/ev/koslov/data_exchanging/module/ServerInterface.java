package ev.koslov.data_exchanging.module;


import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;
import ev.koslov.data_exchanging.components.ResponseBody;
import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.exceptions.UnknownTargetException;

import java.io.IOException;


public abstract class ServerInterface extends AbstractEndpointInterface<Server> {

    public Server startServer(int port) throws IOException {
        if (super.getAssociatedEndpoint() != null && super.getAssociatedEndpoint().isRunning()){
            throw new UnsupportedOperationException("Server is already running.");
        }

        //just creating server instance. It will be associated with current interface in constructor
        return new Server(port, this);
    }

    public boolean isServerRunning() {
        Server server = getAssociatedEndpoint();
        return server != null && server.isRunning();
    }

    public void stopServer() {
        Server server = getAssociatedEndpoint();
        if (server != null && server.isRunning()){
            server.shutdown();
        }
    }

    @Override
    final boolean isResponse(Message responseMessage) {
        return responseMessage.getHeader().getMessageType() == MessageTypeTag.CLIENT_TO_SERVER_RESPONSE;
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
    final void send(Message messageToSend) throws IOException {
        ServerConnection connection = getAssociatedEndpoint().getConnection(messageToSend.getHeader().getTargetId());
        if (connection != null && connection.isConnected()){
            connection.sendMessage(messageToSend);
        } else {
            throw new UnknownTargetException();
        }
    }

    public void forward(Message message) throws IOException {
        send(message);
    }

    public void serverToClientRequest(long targetId, RequestBody requestBody) throws IOException {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.SERVER_TO_CLIENT_REQUEST);
        message.getHeader().setTargetId(targetId);
        message.setBody(requestBody);
        send(message);
    }

    public ResponseBody serverToClientRequest(long targetId, RequestBody requestBody, long timeout) throws InterruptedException, IOException {
        Message message = new Message();
        message.getHeader().setMessageType(MessageTypeTag.SERVER_TO_CLIENT_REQUEST);
        message.getHeader().setTargetId(targetId);
        message.setBody(requestBody);
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
