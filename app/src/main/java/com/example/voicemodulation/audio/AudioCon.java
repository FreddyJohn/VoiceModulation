package com.example.voicemodulation.audio;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.voicemodulation.audio.util.Convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioCon {

    public static class IO_RAF {
        private String file_path;

        public IO_RAF(String file_path) {
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
            if (!new_file)
            {
                try {
                    writeObject.seek(writeObject.length());
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
    public static class IO_F
    {
        public static FileOutputStream setFileOutputStream(String filePath) {
            FileOutputStream out = null;
            try {
                 out = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                 e.printStackTrace();
            }
                return out; }
        public static void closeFileOutputStream(FileOutputStream out, byte[] data) {
        try {
            out.write(data, 0, data.length);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public static class Data{
        @RequiresApi(api = Build.VERSION_CODES.O)
        public static short[] getShorts(String filePath){
            byte[] bytes =getBytes(filePath);
            short[] shorts = Convert.getShortsFromBytes(bytes);
            return shorts;
        }
        public static byte[] getBytes(String filePath) {
            File file = new File(filePath);
            byte[] track = new byte[(int) file.length()];
            FileInputStream in;
            try {
                in = new FileInputStream(file);
                in.read(track);
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return track;
        }
    }
}
