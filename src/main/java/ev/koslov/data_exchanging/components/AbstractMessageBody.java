package ev.koslov.data_exchanging.components;

import java.io.*;
import java.util.Properties;
import java.util.Set;


abstract class AbstractMessageBody implements Serializable {
    private final Properties properties;

    AbstractMessageBody() {
        this.properties = new Properties();
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
