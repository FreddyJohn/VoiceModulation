package com.example.voicemodulation;

import android.media.AudioFormat;

import com.example.voicemodulation.audio.util.Convert;
import com.example.voicemodulation.audio.util.Generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//TODO remove redundancy ->  all time domain modulations
//TODO test performance -> manipulate all then write all VS manipulate one and write one
//TODO rename class to reflect only time domain operations
//TODO move class into a package named to reflect all types of modulations
//TODO test
public class ModulateLogic {
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static int PLAYBACK_SAMPLE_RATE;
    private static int SELECTED_AUDIO_ENCODING;
    private static String CREATION_NAME;
    private FileOutputStream out;
    private byte[] track;

    ModulateLogic(int _PLAYBACK_SAMPLE_RATE, int _SELECTED_AUDIO_ENCODING, String _SELECTED_FILE_NAME) {
        this.SELECTED_AUDIO_ENCODING = _SELECTED_AUDIO_ENCODING;
        this.PLAYBACK_SAMPLE_RATE = _PLAYBACK_SAMPLE_RATE;
        this.CREATION_NAME = _SELECTED_FILE_NAME;

    }

    public void setFileOutputStream(String filePath) {
        try {
            this.out = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeFileOutputStream(short[] data) {
        byte[] bytes = Convert.getBytesFromShorts(data);
        try {
            out.write(bytes, 0, bytes.length);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytesFromTrack() {
        File file = new File(CREATION_NAME);
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

    public short[] getAudioData() {
        setFileOutputStream("/sdcard/Music/test.pcm");
        byte[] bytes = getBytesFromTrack();
        short[] shorts = Convert.getShortsFromBytes(bytes);
        return shorts;
    }

    public void makeBackwardsCreation() {
        short[] frontwards = getAudioData();
        short[] backwards = new short[frontwards.length];
        int size = frontwards.length;
        for (int i = 0; i < size; i++) {
            backwards[i] = frontwards[size - i - 1];
        }
        closeFileOutputStream(backwards);
    }

    public void makePhaserCreation(int frequency) {
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.sine(1, frequency, carrier_wave.length);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) (carrier_wave[i] * modulation_wave[i]);
        }
        closeFileOutputStream(result);
    }

    public void makeRoboticCreation() throws IOException {
        setFileOutputStream("//sdcard/Music/test.pcm");
        byte[] bytes = getBytesFromTrack();
        byte[] one_sample_delay = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i += 32) {
            one_sample_delay[i] = track[i];
            one_sample_delay[i + 1] = track[i + 1];
        }
        out.write(one_sample_delay, 0, one_sample_delay.length);
        out.close();
    }

    public void makeEchoCreation(int delay, int num_signals) {
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double echo_sample = 0;
                for (int signal = 0; signal < num_signals + 1; signal++) {
                    echo_sample += .1 * carrier_wave[(int) (i - delay * Math.pow(delay, signal))];
                }
                result[i] = (short) echo_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (.1 * carrier_wave[i]);
            }
        }
        closeFileOutputStream(result);
    }

    public void makeFlangerCreation(int min, int max, int frequency) {
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double flanger_sample = .1 * carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * Math.sin(frequency * i) + .5) + min)];
                System.out.println(flanger_sample);
                result[i] = (short) flanger_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (.1 * carrier_wave[i]);
            }

        }
        closeFileOutputStream(result);
    }

    public void makeSquaredCreation() {
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            if (carrier_wave[i] > 0) {
                result[i] = (short) Math.sqrt(carrier_wave[i]);
            }
            if (carrier_wave[i] < 0) {
                result[i] = (short) (-1 * Math.sqrt(Math.abs(carrier_wave[i])));
            }
        }
        closeFileOutputStream(result);
    }
}
