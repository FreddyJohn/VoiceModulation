package com.example.voicemodulation.util;

import android.content.Context;
import android.util.DisplayMetrics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Convert {

    public static short[] bytesToShorts(byte[] track) {
        short[] shorts = new short[track.length / 2];
        ByteBuffer.wrap(track).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static byte[] shortsToBytes(short[] track) {
        byte[] bytes = new byte[track.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(track);
        return bytes;
    }
    public static int[] bytesToInts(byte[] track) {
        int[] ints = new int[track.length / 4];
        ByteBuffer.wrap(track).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(ints);
        return ints;
    }

    public static byte[] IntsToBytes(int[] ints) {
        byte[] bytes = new byte[ints.length * 4];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(ints);
        return bytes;
    }
    public static float[] shortBytesToFloats(byte[] track) {
        float[] floatyShorts = new float[track.length / 2];
        for (int i = 0; i < track.length/2; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(track[i+i]);
            buffer.put(track[i+i+1]);
            floatyShorts[i]=buffer.getShort(0);
        }
        return floatyShorts;
    }

    public static int numberToDp(final Context context,float num){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float fpixels = metrics.density * num;
        return (int) (fpixels + 0.5f);
    }
    public static float dpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}
