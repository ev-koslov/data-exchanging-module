package ev.koslov.data_exchanging.components;


import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.components.tags.StatusTag;

import java.io.IOException;

public final class MessageFabric {

    public Message serverToClientRequestMessage(long targetId, RequestMessageBody requestMessageBody) throws IOException {
        Message message = prepareMessage(MessageTypeTag.SERVER_TO_CLIENT_REQUEST);
        message.setTargetId(targetId);
        message.setBody(requestMessageBody);
        return message;
    }

    public Message clientToServerRequestMessage(RequestMessageBody requestMessageBody) throws IOException {
        Message message = prepareMessage(MessageTypeTag.CLIENT_TO_SERVER_REQUEST);
        message.setBody(requestMessageBody);
        return message;
    }

    public Message clientToClientRequestMessage(long targetId, RequestMessageBody requestMessageBody) throws IOException {
        Message message = prepareMessage(MessageTypeTag.CLIENT_TO_CLIENT_REQUEST);
        message.setTargetId(targetId);
        message.setBody(requestMessageBody);
        return message;
    }

    public Message responseMessage(Message request, StatusTag statusTag, ResponseMessageBody messageBody) throws IOException {
        Message responseMessage = new Message();

        switch (request.getMessageType()) {
            case SERVER_TO_CLIENT_REQUEST: {
                responseMessage.setMessageType(MessageTypeTag.CLIENT_TO_SERVER_RESPONSE);
                break;
            }
            case CLIENT_TO_SERVER_REQUEST: {
                responseMessage.setMessageType(MessageTypeTag.SERVER_TO_CLIENT_RESPONSE);
                break;
            }
            case CLIENT_TO_CLIENT_REQUEST: {
                responseMessage.setMessageType(MessageTypeTag.CLIENT_TO_CLIENT_RESPONSE);
                break;
            }
        }

        responseMessage.setTargetId(request.getSourceId());
        responseMessage.setStatus(statusTag);
        responseMessage.setResponseForRequestId(request.getRequestId());

        responseMessage.setBody(messageBody);

        return responseMessage;
    }

    private Message prepareMessage(MessageTypeTag typeTag) {
        Message message = new Message();
        message.setMessageType(typeTag);
        return message;
    }
}
