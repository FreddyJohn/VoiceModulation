package com.example.voicemodulation.controls;

import android.media.AudioFormat;

public class ControlCases {
    public static int encodingSeeker(int progress) {
        int encoding=0;
        switch (progress) {

            case 1:
                encoding = AudioFormat.ENCODING_PCM_16BIT;
                break;
            case 0:
                encoding = AudioFormat.ENCODING_PCM_8BIT;
                break;
            default:
                encoding = AudioFormat.ENCODING_DEFAULT;
        }
        return encoding;
    }
    public static class Channels{
        public int in;
        public int out;
    }
    public static Channels channelSeeker(int progress) {
        Channels channels = new Channels();
        switch (progress) {
            case 1:
                channels.in=AudioFormat.CHANNEL_IN_STEREO;
                channels.out=AudioFormat.CHANNEL_OUT_STEREO;
                break;
            case 0:
                channels.in=AudioFormat.CHANNEL_IN_MONO;
                channels.out=AudioFormat.CHANNEL_OUT_MONO;
                break;
        }
        return channels;
    }
    public static String formatSeeker(int progress) {
        String format ="";
        switch (progress) {
            case 2:
                format = ".wav";
                break;
            case 1:
                format = ".mp4";
                break;
            case 0:
                format = ".pcm";
                break;
        }
        return format;
    }
    public interface seekers{
        String quanToType(int quan);
    }
    public static class encodingSeeker implements seekers {
        @Override
        public String quanToType(int quan) {
            String type = "";
            switch (quan) {
                case 1:
                    type = "16Bit";
                    break;
                case 0:
                    type = "8Bit";
                    break;
            }
            return type;
        }
    }
    public static class formatSeeker implements seekers {
        @Override
        public String quanToType(int quan) {
            String format ="";
            switch (quan) {
                case 2:
                    format = ".wav";
                    break;
                case 1:
                    format = ".mp4";
                    break;
                case 0:
                    format = ".pcm";
                    break;
            }
            return format;
        }

    }
    public static class channelSeeker implements seekers {

        @Override
        public String quanToType(int quan) {
            String channels="";
            switch (quan) {
                case 1:
                    channels="Stereo";
                    break;
                case 0:
                    channels="Mono";
                    break;
            }
            return channels;
        }

    }
}
