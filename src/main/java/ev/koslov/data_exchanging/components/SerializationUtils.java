package ev.koslov.data_exchanging.components;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by voron on 21.02.2017.
 */
public class SerializationUtils {

    public static String marshall(Object object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            marshaller.marshal(object, bos);
            return new String(bos.toByteArray());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }

    public static <T> T unmarshall(String xml, Class<T> expectedClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(expectedClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = null;
        try {
            reader = new StringReader(xml);
            return (T) unmarshaller.unmarshal(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static byte[] serialize(Serializable object) throws IOException {
        ByteArrayOutputStream bos = null;
        ZipOutputStream zos = null;
        ObjectOutputStream ous = null;
        try {
            bos = new ByteArrayOutputStream();
            zos = new ZipOutputStream(bos);
            ous = new ObjectOutputStream(zos);

            zos.putNextEntry(new ZipEntry(object.getClass().getName()));
            ous.writeObject(object);
            zos.closeEntry();

            return bos.toByteArray();
        } finally {
            if (ous != null) {
                try {
                    ous.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public static <T> T deserialize(byte[] serializedData) throws IOException {
        ByteArrayInputStream bis = null;
        ZipInputStream zis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(serializedData);
            zis = new ZipInputStream(bis);
            ois = new ObjectInputStream(zis);

            zis.getNextEntry();
            Object deserializedObject = ois.readObject();
            zis.closeEntry();

            return (T) deserializedObject;

        } catch (ClassNotFoundException e) {
            throw new IOException(e);
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
}
