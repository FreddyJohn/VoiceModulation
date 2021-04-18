package com.example.voicemodulation.audio;

import com.example.voicemodulation.audio.util.Convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
public class AudioCon {

    public static class IO_RAF {
        private final String file_path;

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
                file.seek(offset);
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
        public FileOutputStream setFileOutputStream(String filePath) {
            FileOutputStream out = null;
            try {
                 out = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                 e.printStackTrace();
            }
                return out; }
        public void closeFileOutputStream(FileOutputStream out, byte[] data) {
        try {
            out.write(data, 0, data.length);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
    //TODO this needs to be RandomAccessFile with start stop
    public static class Data{
        public static short[] getShorts(String filePath){
            byte[] bytes =getBytes(filePath);
            short[] shorts = Convert.bytesToShorts(bytes);
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
        public static byte[] getAudioChunk(long file_position,int chunk_size, int n,RandomAccessFile file) {
            try {
                file.seek(file_position);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int length = 0;
            try {
                length = (int) file.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[Math.abs((length * n) - chunk_size)];
            try {
                file.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return buffer;
        }
        public static long getMemory(){
            final Runtime runtime = Runtime.getRuntime();
            final long usedMem=(runtime.totalMemory() - runtime.freeMemory());
            final long maxHeapSize=runtime.maxMemory();
            final long availHeapSize = maxHeapSize - usedMem;
            return availHeapSize;
        }
    }
}
