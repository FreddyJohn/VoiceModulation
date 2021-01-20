package com.example.voicemodulation.audio.util;

public class Generate {
    public static double[] sin(double a, double f, int samples,int fps) {
        int time = samples/fps;
        double[] t = linspace(0, time, samples);
        double[] y = new double[samples];
        for (int x = 0; x < t.length; x++) {
            y[x] = a * Math.sin(f * t[x]);
        }
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
}
