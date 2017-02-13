package ev.koslov.data_exchanging.components;

import java.io.*;


abstract class AbstractMessageBody implements Serializable {

    protected AbstractMessageBody() {

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
