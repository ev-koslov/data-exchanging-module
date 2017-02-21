package ev.koslov.data_exchanging.components;

import ev.koslov.data_exchanging.components.tags.MessageTypeTag;
import ev.koslov.data_exchanging.components.tags.StatusTag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;


//TODO: придумать вариант вычитывания хидера и начинать обработку еще до полной загрузки тела сообщения.
//TODO: например, метод readBody(Message partialMessage), вызываемый, если сообщение получено частично.
//TODO: добавить в сообщение флаг - ЧАСТИЧНОЕ. И в зависимотси от него, вызывать или parse(ByteBuffer rawBuffer) или readBody(Message partialMessage, ByteBuffer rawBuffer)

public final class Message {

    //Message header. Eve message must start from it. Otherwise IOException will be thrown
    private static final String MESSAGE_HEADER = "msg";

    private static final byte[] MESSAGE_HEADER_BYTES = MESSAGE_HEADER.getBytes();
    private static final int MESSAGE_HEADER_LENGTH = MESSAGE_HEADER_BYTES.length;

    //Header size is a sum of HEADER length and int value which represents data (byte[]) size + int tag
    private static final int MESSAGE_META_LENGTH = MESSAGE_HEADER_BYTES.length + 4 + 4 + 8 + 8 + 8 + 8 + 4;

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
                parsedMessage.header.messageType = messageTypeTag;
                parsedMessage.header.status = statusTag;
                parsedMessage.header.targetId = targetId;
                parsedMessage.header.sourceId = sourceId;
                parsedMessage.header.requestId = requestId;
                parsedMessage.header.responseForRequestId = responseForRequestId;

                if (dataSize > 0) {
                    byte[] body = new byte[dataSize];
                    rawBuffer.get(body);
                    parsedMessage.body = body;
                }

            } else {

                //if we got message header, but incomplete body, rewinding buffer, and setting nextMessage size
                rawBuffer.rewind();

            }

        }

        rawBuffer.compact();

        return parsedMessage;
    }


    private final Header header;
    private byte[] body;

    public Message() {
        header = new Header();
        body = new byte[0];
    }

    public Header getHeader() {
        return header;
    }

    public boolean hasBody() {
        return body.length > 0;
    }

    /**
     * Sets message body
     *
     * @param abstractMessageBody data to set
     * @throws IOException if AbstractMessageBody implementation contains data, that could not be serialized.
     */
    public void setBody(AbstractMessageBody abstractMessageBody) throws IOException {
        if (abstractMessageBody != null) {
            try {

                this.body =  SerializationUtils.serialize(abstractMessageBody);

            } catch (IOException e) {

                throw new IOException("Serialization of "+this.getClass().getName()+" failed.", e);

            }
        }
    }

    public <B extends AbstractMessageBody> B getBody() throws IOException {
        if (!hasBody()) {
            return null;
        }

        try {
            return SerializationUtils.deserialize(body);
        } catch (IOException e) {
            throw new IOException("This class cannon be deserialized.", e);
        }
    }

    /**
     * Returns message as read ready byte buffer
     *
     * @return {@link ByteBuffer} that contains message in raw format.
     */
    public final ByteBuffer getAsReadReadyByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_META_LENGTH + body.length);
        buffer.put(MESSAGE_HEADER_BYTES);
        buffer.putInt(header.messageType);
        buffer.putInt(header.status);
        buffer.putLong(header.targetId);
        buffer.putLong(header.sourceId);
        buffer.putLong(header.requestId);
        buffer.putLong(header.responseForRequestId);
        buffer.putInt(body.length);
        buffer.put(body);
        buffer.flip();
        return buffer;
    }


    /**
     * Class describes message header data. This data used for message pre processing
     */
    public class Header {
        private int messageType;
        private int status = 0;

        private long targetId = -1;
        private long sourceId = -1;

        private long requestId = -1;
        private long responseForRequestId = -1;

        private Header() {
        }

        public MessageTypeTag getMessageType() {
            return MessageTypeTag.values()[messageType];
        }

        public void setMessageType(MessageTypeTag messageType) {
            this.messageType = messageType.ordinal();
        }

        public StatusTag getStatus() {
            return StatusTag.values()[status];
        }

        public void setStatus(StatusTag status) {
            this.status = status.ordinal();
        }

        public long getTargetId() {
            return targetId;
        }

        public void setTargetId(long targetId) {
            this.targetId = targetId;
        }

        public long getSourceId() {
            return sourceId;
        }

        public void setSourceId(long sourceId) {
            this.sourceId = sourceId;
        }

        public long getRequestId() {
            return requestId;
        }

        public void setRequestId(long requestId) {
            this.requestId = requestId;
        }

        public long getResponseForRequestId() {
            return responseForRequestId;
        }

        public void setResponseForRequestId(long responseForRequestId) {
            this.responseForRequestId = responseForRequestId;
        }
    }
}
