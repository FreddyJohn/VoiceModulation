package com.example.voicemodulation.audio.util;

import android.content.Context;
import android.util.DisplayMetrics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Convert {

    public static int[] getBitsFromBytes(byte[] track) {
        int size = track.length;
        int[] bits = new int[size];
        for (int i = 0; i < size; i++) {
            bits[i] = track[i];
        }
        return bits;
    }

    public static byte[] getBytesFromBits(int[] bits) {
        int size = bits.length;
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) bits[i];
        }
        return bytes;
    }

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

    public static int numberToDp(final Context context,float num){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float fpixels = metrics.density * num;
        int pixels = (int) (fpixels + 0.5f);
        return pixels;
    }


}
