package com.example.voicemodulation.audio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.RandomAccessFile;

public class AudioCon {

    public static class IO {
        private String file_path;

        public IO(String file_path) {
            this.file_path = file_path;
        }

        public RandomAccessFile getWriteObject(boolean new_file) {
            RandomAccessFile writeObject = null;
            try {
                writeObject = new RandomAccessFile(file_path, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (new_file) {
                try {
                    writeObject.setLength(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return writeObject;
        }

        public RandomAccessFile getReadObject() {
            RandomAccessFile readObject = null;
            try {
                readObject = new RandomAccessFile(file_path, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return readObject;
        }

        public void setSeekToPointer(RandomAccessFile file, int offset) {
            try {
                file.seek(file.length() + offset);
                System.out.println("the file length is: " + file.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public long getObjectLength(RandomAccessFile file) {
            long length = -1;
            try {
                file.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return length;
        }

    }

    public static class Pipes {
        public PipedReader getReaderObject() {
            PipedReader reader = new PipedReader();
            return reader;
        }

        public PipedWriter getWriterObject() {
            PipedWriter writer = new PipedWriter();
            return writer;
        }

        public void connectPipes(PipedReader reader, PipedWriter writer) {
            try {
                writer.connect(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
