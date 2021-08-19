package com.example.voicemodulation.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

public class Persist<T> {

    private T t;
    private RandomAccessFile outputFile;


    public void setOutputFile(String file){
        try {
            outputFile = new RandomAccessFile(file,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public T serialize(T t) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out;
            byte[] bytes;
            out = new ObjectOutputStream(bos);
            out.writeObject(t);
            out.flush();
            bytes = bos.toByteArray();
            outputFile.setLength(0);
            outputFile.seek(0);
            outputFile.write(bytes);
        } catch (IOException ex) {
        }
        return t;
    }

    public T deserialize() {
        try {
            int object_length = (int) outputFile.length();
            byte[] object = new byte[object_length];
            outputFile.seek(0);
            outputFile.read(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(object);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            t = (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
        }
        return t;
    }
}
