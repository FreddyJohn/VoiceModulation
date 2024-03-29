package com.adams.voicemodulation.util;

import android.content.Context;
import android.util.DisplayMetrics;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;

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

    public static byte[] intsToBytes(int[] ints) {
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

    public static float numberToDp(final Context context,float num){
        /*
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float fpixels = metrics.density * num;
        return (int) (fpixels + 0.5f);
         */
        return context.getResources().getDisplayMetrics().density*num;
    }
    public static float dpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float pixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    public static String time(long pTime) {
        final long min = pTime/60;
        final long sec = pTime-(min*60);
        final String strMin = placeZeroIfNeed((int) min);
        final String strSec = placeZeroIfNeed((int) sec);
        return String.format("%s:%s",strMin,strSec);
    }
    private static String placeZeroIfNeed(int number) {
        return (number >=10)? Integer.toString(number):String.format("0%s",Integer.toString(number));
    }

    public static String memory(long numBytes) {
        String formatted = "";
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        String commaNum =format.format(numBytes);
        if (numBytes / 1E6 < 1) {
            int kB = (int) (numBytes / 1E3);
            formatted = kB + " kB" + " (" + commaNum + " bytes)";
        } else if (numBytes / 1E6 > 1) {
            int mB = (int) (numBytes / 1E6);
            formatted = mB + " mB" + " (" + commaNum + " bytes)";
        }
        return formatted;
    }
}
