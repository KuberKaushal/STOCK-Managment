package com.vityarthi.inventory.util;

import java.io.*;

/**
 * Utility for persisting and loading object graphs using Java Object Serialization.
 */
public final class DataSerializer {

    private DataSerializer() {}

    public static synchronized void serialize(Serializable object, String targetFilePath) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("Cannot serialize null object.");
        }
        File targetFile = new File(targetFilePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        File tempFile = new File(targetFilePath + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
        }

        if (targetFile.exists()) {
            if (!targetFile.delete()) {
                throw new IOException("Failed to replace existing data file: " + targetFilePath);
            }
        }
        if (!tempFile.renameTo(targetFile)) {
            throw new IOException("Failed to rename temporary serialization file to: " + targetFilePath);
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> T deserialize(String sourceFilePath) throws IOException, ClassNotFoundException {
        File file = new File(sourceFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Data file does not exist: " + sourceFilePath);
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        }
    }
}
