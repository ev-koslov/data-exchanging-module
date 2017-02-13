package ev.koslov.data_exchanging.components;

import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.components.tags.StatusTag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;


//TODO: придумать вариант вычитывания хидера и начинать обработку еще до полной загрузки тела сообщения.
//TODO: например, метод readBody(Message partialMessage), вызываемый, если сообщение получено частично.
//TODO: добавить в сообщение флаг - ЧАСТИЧНОЕ. И в зависимотси от него, вызывать или parse(ByteBuffer rawBuffer) или readBody(Message partialMessage, ByteBuffer rawBuffer)

public final class Message {
    public static int TARGET_SERVER = 0;

    //Message header. Eve message must start from it. Otherwise IOException will be thrown
    private static final String MESSAGE_HEADER = "msg";

    private static final byte[] MESSAGE_HEADER_BYTES = MESSAGE_HEADER.getBytes();
    private static final int MESSAGE_HEADER_LENGTH = MESSAGE_HEADER_BYTES.length;

    //Header size is a sum of HEADER length and int value wich represents data (byte[]) size + int tag
    private static final int MESSAGE_META_LENGTH = MESSAGE_HEADER_BYTES.length  + 4 + 4 + 8 + 8 + 8 + 8 + 4;

    /**
     * Parses message from ByteBuffer that contains raw serializedBody and compacts buffer if message was parsed
     *
     * @param rawBuffer buffer that contains RAW data to append for processing
     * @return parsed {@link Message}
     * @throws ParseException if message is containing invalid data
     */
    public static Message parse(ByteBuffer rawBuffer) throws ParseException {
        Message parsedMessage = null;

        //make buffer read ready
        rawBuffer.flip();

        //trying to read message
        if (rawBuffer.remaining() >= MESSAGE_META_LENGTH) {

            //reading message header
            byte[] receivedHeader = new byte[MESSAGE_HEADER_LENGTH];

            rawBuffer.get(receivedHeader);

            //if header is wrong throwing IOException
            if (!Arrays.equals(receivedHeader, Message.MESSAGE_HEADER_BYTES)) {
                throw new ParseException("Wrong message header data: " + new String(receivedHeader), 0);
            }

            //reading message type
            int messageTypeTag = rawBuffer.getInt();
            //reading status
            int statusTag = rawBuffer.getInt();
            //reading targetId
            long targetId = rawBuffer.getLong();
            //reading targetId
            long sourceId = rawBuffer.getLong();
            //reading requestId
            long requestId = rawBuffer.getLong();
            //reading responseForRequestId
            long responseForRequestId = rawBuffer.getLong();
            //reading message size as int
            int dataSize = rawBuffer.getInt();

            //if buffer has more or equals serializedBody remaining than message size
            //preparing message
            if (rawBuffer.remaining() >= dataSize) {

                //creating message object
                parsedMessage = new Message();
                parsedMessage.messageType = messageTypeTag;
                parsedMessage.status = statusTag;
                parsedMessage.targetId = targetId;
                parsedMessage.sourceId = sourceId;
                parsedMessage.requestId = requestId;
                parsedMessage.responseForRequestId = responseForRequestId;

                if (dataSize > 0) {
                    byte[] body = new byte[dataSize];
                    rawBuffer.get(body);
                    parsedMessage.serializedBody = body;
                }

            } else {

                //if we got message header, but incomplete body, rewinding buffer, and setting nextMessage size
                rawBuffer.rewind();

            }

        }

        rawBuffer.compact();

        return parsedMessage;
    }


    private int messageType;
    private int status = 0;

    private long targetId;
    private long sourceId;

    private long requestId = -1;
    private long responseForRequestId = -1;

    private byte[] serializedBody = new byte[0];

    protected Message() {

    }

//    public Message(MessageTypeTag messageType, StatusTag status, long targetId) {
//        this.messageType = messageType.ordinal();
//        this.status = status.ordinal();
//        this.targetId = targetId;
//    }
//
//    public Message(MessageTypeTag messageType, StatusTag status, long targetId, AbstractMessageBody messageBody) throws IOException {
//        this(messageType, status, targetId);
//        this.serializedBody = messageBody.getBytes();
//    }
//
//    public Message(MessageTypeTag messageType, StatusTag status, long targetId, long responseForRequestId) {
//        this(messageType, status, targetId);
//        this.responseForRequestId = responseForRequestId;
//    }
//
//    public Message(MessageTypeTag messageType, StatusTag status, long targetId, long responseForRequestId, AbstractMessageBody messageBody) throws IOException {
//        this(messageType, status, targetId, responseForRequestId);
//        this.serializedBody = messageBody.getBytes();
//    }

    public boolean hasBody(){
        return serializedBody.length > 0;
    }

    public <T extends Enum<T>, B extends AbstractMessageBody> B deserializeBody() throws IOException, ClassNotFoundException {

        if (serializedBody.length == 0){
            return null;
        }

        ByteArrayInputStream bis;
        ObjectInputStream ois = null;

        try {

            bis = new ByteArrayInputStream(serializedBody);
            ois = new ObjectInputStream(bis);

            return (B) ois.readObject();

        } finally {

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }

    public MessageTypeTag getMessageType() {
        return MessageTypeTag.values()[messageType];
    }

    public StatusTag getStatus() {
        return StatusTag.values()[status];
    }

    public long getTargetId() {
        return targetId;
    }

    public long getSourceId() {
        return sourceId;
    }

    public long getRequestId() {
        return requestId;
    }

    public long getResponseForRequestId() {
        return responseForRequestId;
    }


    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    void setRequestId(long requestId) {
        this.requestId = requestId;
    }



    void setMessageType(MessageTypeTag messageType) {
        this.messageType = messageType.ordinal();
    }

    void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    void setResponseForRequestId(long responseForRequestId) {
        this.responseForRequestId = responseForRequestId;
    }

    public Message setStatus(StatusTag status) {
        this.status = status.ordinal();
        return this;
    }

    public Message setBody(AbstractMessageBody abstractMessageBody) throws IOException {
        this.serializedBody = abstractMessageBody.getBytes();
        return this;
    }

    /**
     * Returns message as read ready byte buffer
     *
     * @return {@link ByteBuffer} that contains message in raw format.
     */
    public final ByteBuffer getAsReadReadyByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_META_LENGTH + serializedBody.length);
        buffer.put(MESSAGE_HEADER_BYTES);
        buffer.putInt(messageType);
        buffer.putInt(status);
        buffer.putLong(targetId);
        buffer.putLong(sourceId);
        buffer.putLong(requestId);
        buffer.putLong(responseForRequestId);
        buffer.putInt(serializedBody.length);
        buffer.put(serializedBody);
        buffer.flip();
        return buffer;
    }

}
