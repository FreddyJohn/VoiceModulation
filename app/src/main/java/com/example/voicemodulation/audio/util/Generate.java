package com.example.voicemodulation.audio.util;

import java.lang.ref.Reference;

public class Generate {
    public static double[] sin(double a, double f, double p, int samples, int fps) {
        int time = samples/fps;
        double[] t = linspace(0, time, samples);
        double[] y = new double[samples];
        for (int x = 0; x < t.length; x++) {
            y[x] = a * Math.sin(f * t[x]+p);
        }
        return y;
    }

    public static double[] triangle(int a, double p,double theta, int samples, int fps)
    {
        int time = samples/fps;
        double[] t = linspace(0, time, samples);
        double[] y = new double[samples];
        for (int x = 0; x < t.length; x++) {
            y[x]= ((2*a)/Math.PI) * Math.asin(Math.sin((2*Math.PI*x)/p)+theta);
        }
        return y;
    }
    public static double triangle_t(double t)
    {
        double y= ((2*1)/Math.PI) * Math.asin(Math.sin((2*Math.PI*t)/1));
        return y;
    }
    public static double[] saw(int a, double p,double theta, int samples, int fps)
    {
        int time = samples/fps;
        double[] t = linspace(0, time, samples);
        double[] y = new double[samples];
        for (int x = 0; x < t.length; x++) {
            y[x]= -((2*a)/Math.PI) * Math.atan(1/Math.tan((2*Math.PI*x)/p)+theta);
        }
        return y;
    }
    public static double[] square(int a, double f,double theta, int samples, int fps)
    {
        int time = samples/fps;
        double[] t = linspace(0, time, samples);
        double[] y = new double[samples];
        for (int x = 0; x < t.length; x++) {
            y[x]= a*Math.signum(Math.sin(2*Math.PI*f*x+theta));
        }
        return y;
    }
    public static double square_t(double t)
    {
        double y = Math.signum(Math.sin(2*Math.PI*t));
        return y;
    }
    public static double[] linspace(double min, double max, int points) {
        double[] d = new double[points];
        for (int i = 0; i < points; i++) {
            d[i] = min + i * (max - min) / (points - 1);
        }
        return d;
    }
    public static int mixEquation(int a, int b, int range) {
        int y;
        if (a <= 0 || b <= 0) {
            y = a + b;
        } else {
            y = a + b - ((a * b) / range);
        }
        return y;
    }
    public static double getNormalizationCoefficient(short[] data){
        double max=1;
        for (int counter = 1; counter < data.length; counter++)
        {
            int sample = Math.abs(data[counter]);
            if (sample > Math.abs(max))
            {
                max = sample;
            }
        }
        return (max/65535)/1.7;
    }
    public static short getAbsoluteMax(byte[] buffer){
        short[] data=Convert.getShortsFromBytes(buffer);
        short max=0;
        for (int counter = 1; counter < data.length; counter++)
        {
            int sample = Math.abs(data[counter]);
            if (sample > Math.abs(max))
            {
                max = data[counter];
                //System.out.println(max);
            }
        }
        return max;
    }
    public static byte[] wavHeader(
            long totalAudioLen, long totalDataLen, long sampleRate, int channels,byte RECORDER_BPP) {
        //byteRate=SampleRate * NumChannels * BitsPerSample/8
        long byteRate=sampleRate*1*(RECORDER_BPP/8);
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; //16 for PCM. 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) 1; // channels
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (1 * (RECORDER_BPP / 8)); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }
}
