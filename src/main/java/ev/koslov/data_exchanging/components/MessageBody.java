package ev.koslov.data_exchanging.components;

import java.io.*;


public class MessageBody<T extends Enum<T>> implements Serializable {
    private T command;

    public MessageBody(T command) {
        //TODO: add checking for T implements Taglib interface. Throw exception otherwise
        this.command = command;
    }

    public T getCommand() {
        return command;
    }

    protected final byte[] getBytes() throws IOException {
        ByteArrayOutputStream bos;
        ObjectOutputStream ous = null;

        try {
            bos = new ByteArrayOutputStream();
            ous = new ObjectOutputStream(bos);

            ous.writeObject(this);

            return bos.toByteArray();

        } finally {
            if (ous != null) {
                try {
                    ous.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }
}
