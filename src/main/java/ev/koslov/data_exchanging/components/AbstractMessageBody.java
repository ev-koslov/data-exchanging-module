package ev.koslov.data_exchanging.components;

import java.io.*;
import java.util.Properties;
import java.util.Set;


abstract class AbstractMessageBody implements Serializable {
    private final Properties properties;

    AbstractMessageBody() {
        this.properties = new Properties();
    }

    final byte[] getBytes() {
        ByteArrayOutputStream bos;
        ObjectOutputStream ous = null;

        try {
            bos = new ByteArrayOutputStream();
            ous = new ObjectOutputStream(bos);

            ous.writeObject(this);

            return bos.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException("Serialization of "+this.getClass().getName()+" failed.", e);

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

    public <S extends Serializable> S getProperty(String key) {
        return (S) properties.get(key);
    }

    public void setProperty(String key, Serializable value) {
        properties.put(key, value);
    }

    public Set<String> propertyNames() {
        return properties.stringPropertyNames();
    }

    public int propertiesSize() {
        return properties.size();
    }
}
