package com.example.voicemodulation;
import android.app.Activity;
import android.media.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.io.*;

public class ModulateLogic
{
    private static int PLAYBACK_SAMPLE_RATE;
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static int SELECTED_AUDIO_ENCODING;
    private static String CREATION_NAME;
    private FileOutputStream  out;
    private byte[] track;
    public Activity activity;
    ModulateLogic(Activity _activity, int _PLAYBACK_SAMPLE_RATE,int _SELECTED_AUDIO_ENCODING,String _SELECTED_FILE_NAME)
    {
        this.SELECTED_AUDIO_ENCODING=_SELECTED_AUDIO_ENCODING;
        this.PLAYBACK_SAMPLE_RATE=_PLAYBACK_SAMPLE_RATE;
        this.CREATION_NAME=_SELECTED_FILE_NAME;
        this.activity=_activity;
    }
    public void setFileOutputStream(String filePath)
    {
        try {
            this.out = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public int mixEquation(int a, int b)
    {
        int y=a+b/2;
        return y;
    }
    public float[] getFloatsFromBytes(byte[] track)
    {
        float[] floats = new float[track.length];
        for (int i=4;i<track.length;i+=4)
        {
            byte[] bytes;
            bytes= Arrays.copyOfRange(track,i-4,i);
            floats[i]= ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return floats;
    }
    public int[] getBitsFromBytes(byte[] track)
    {
        int size =track.length;
        int[] bits = new int[size];
        for (int i=0; i<size; i++)
        {
            bits[i]=track[i];
        }
        return bits;
    }

    public byte[] getBytesFromBits(int[] bits)
    {
        int size = bits.length;
        byte[] bytes=new byte[size];
        for (int i =0; i<size;i++)
        {
            bytes[i]=(byte)bits[i];
        }
        return bytes;
    }
    public byte[] getBytesFromTrack() throws IOException {
        File file = new File(CREATION_NAME);
        track = new byte[(int) file.length()];
        FileInputStream in;
        try
        {
            in = new FileInputStream(file);
            in.read(track);
            in.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return track;
    }

    public byte[] getBytesFromFloats(float[] floats)
    {
        int size = floats.length;
        byte[] bytes = new byte[size];
        for (int i=0; i<size; i++)
        {
            byte[] buffer;
            buffer=ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(floats[i]).array();
            for (int j=0; j<buffer.length;j++)
            {
                bytes[i] = buffer[j];
            }
        }
        return bytes;
    }
    public void makeBackwardsCreation() throws IOException {
        setFileOutputStream("/sdcard/Music/test.pcm");
        byte[] frontwards_bytes=getBytesFromTrack();
        float[] frontwards=getFloatsFromBytes(frontwards_bytes);
        float[] backwards = new float[frontwards.length];
        int size=frontwards.length;
        for (int i=0;i<size;i++)
        {
            backwards[i] = frontwards[size-i-1];
        }
        byte[] backwards_bytes=getBytesFromFloats(backwards);
        out.write(backwards_bytes,0,backwards.length);
        out.close();
    }
    public void makeEchoCreation() throws IOException {
        int[] echo;
        setFileOutputStream("/sdcard/Music/test.pcm");
        byte[] bytes=getBytesFromTrack();
        int[] track=getBitsFromBytes(bytes);
        echo = new int[track.length];
        int size=track.length;
        int offset = size/3;
        int count=0;
        for (int i=0; i<size; i++)
        {
            count+=1;
            if (count<offset)
            {
                echo[i]=track[i];
            }
            if (count>offset)
            {
                int mix = mixEquation(track[i],track[i-offset]);
                echo[i]=mix;
            }
        }
        byte[] echo_bytes=getBytesFromBits(echo);
        out.write(echo_bytes,0,echo.length);
        out.close();
    }
}
