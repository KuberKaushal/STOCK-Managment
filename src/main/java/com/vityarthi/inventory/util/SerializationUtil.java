package com.vityarthi.inventory.util;

import java.io.*;

public final class SerializationUtil {

    private SerializationUtil() {}

    public static synchronized <T extends Serializable> void serialize(T object, String filePath) throws IOException {
        if (object == null) throw new IllegalArgumentException("Cannot serialize null object.");
        File target = new File(filePath);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        File tempFile = new File(filePath + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
        }

        if (target.exists() && !target.delete()) {
            throw new IOException("Failed to delete existing target file: " + filePath);
        }
        if (!tempFile.renameTo(target)) {
            throw new IOException("Failed to rename temporary file to: " + filePath);
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Serializable> T deserialize(String filePath, Class<T> clazz)
            throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) throw new FileNotFoundException("Serialized file not found: " + filePath);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object obj = ois.readObject();
            if (clazz.isInstance(obj)) {
                return (T) obj;
            } else {
                throw new ClassCastException("Expected instance of " + clazz.getName() + " but found " + obj.getClass().getName());
            }
        }
    }
}
