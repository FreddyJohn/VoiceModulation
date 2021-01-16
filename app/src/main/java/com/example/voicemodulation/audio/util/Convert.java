package com.example.voicemodulation.audio.util;

import android.content.Context;

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

    public static short[] getShortsFromBytes(byte[] track) {
        short[] shorts = new short[track.length / 2];
        ByteBuffer.wrap(track).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static byte[] getBytesFromShorts(short[] track) {
        byte[] bytes = new byte[track.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(track);
        return bytes;
    }
    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
