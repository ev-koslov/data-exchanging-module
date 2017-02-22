package ev.koslov.data_exchanging.components;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


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
        //TODO: add compression
        ByteArrayOutputStream bos;
        ObjectOutputStream ous = null;
        try {
            bos = new ByteArrayOutputStream();
            ous = new ObjectOutputStream(bos);

            ous.writeObject(object);

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

    public static <T> T deserialize(byte[] serializedData) throws IOException {
        ByteArrayInputStream bis;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(serializedData);
            ois = new ObjectInputStream(bis);

            Object deserializedObject = ois.readObject();

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

    public static byte[] compress(byte[] data) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);

        try {
            zos.putNextEntry(new ZipEntry(""));
            zos.write(data);
            zos.closeEntry();

            return bos.toByteArray();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                //do nothing
            }
            try {
                bos.close();
            } catch (IOException e) {
                //do nothing
            }
        }
    }

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ZipInputStream zis = new ZipInputStream(bis);

        try {
            zis.getNextEntry();
            byte[] decompressedData = new byte[zis.available()];
            zis.read(decompressedData);
            zis.closeEntry();
            return decompressedData;
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                //do nothing
            }
            try {
                bis.close();
            } catch (IOException e) {
                //do nothing
            }
        }
    }
}
