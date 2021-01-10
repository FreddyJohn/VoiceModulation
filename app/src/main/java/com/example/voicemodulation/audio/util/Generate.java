package com.example.voicemodulation.audio.util;

public class Generate {
    public static double[] sine(double a, double f, int l) {
        double[] t = linspace(0, 50, l);
        double[] y = new double[l];
        for (int i = 0; i < t.length; i++) {
            y[i] = a * Math.sin(f * t[i]);
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
