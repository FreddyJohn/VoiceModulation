package com.example.voicemodulation.audio;
import android.media.AudioFormat;
import java.util.Arrays;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.util.Convert;
import com.example.voicemodulation.audio.util.Generate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ModulateLogic {
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static String MODULATION_NAME;
    private static int PLAYBACK_SAMPLE_RATE;
    private static int SELECTED_AUDIO_ENCODING;
    private static String CREATION_NAME;
    private static FileOutputStream out;
    private static double[] params;
    private static double n;
    public ModulateLogic(double[] _params, AudioFile recording){
        params=_params;
        CREATION_NAME = recording.getNewRecordFile();
        MODULATION_NAME = recording.getNewModulateFile();
        PLAYBACK_SAMPLE_RATE = recording.getSampleRate();}
    public static void setFileOutputStream(String filePath) {
        try {
            out = new FileOutputStream(filePath); }
        catch (FileNotFoundException e) { e.printStackTrace(); } }
    public static void closeFileOutputStream(short[] data) {
        byte[] bytes = Convert.getBytesFromShorts(data);
        try {
            out.write(bytes, 0, bytes.length);
            out.close(); }
        catch (IOException e) { e.printStackTrace(); } }
    public static byte[] getBytesFromTrack() {
        File file = new File(CREATION_NAME);
        byte[] track = new byte[(int) file.length()];
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(track);
            in.close(); }
        catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        return track; }
    public static short[] getAudioData() {
        byte[] bytes = getBytesFromTrack();
        setFileOutputStream(MODULATION_NAME);
        short[] shorts = Convert.getShortsFromBytes(bytes);
        n=Generate.getNormalizationCoefficient(shorts);
        System.out.println("ALL THAT TALK:"+n);
        return shorts; }

    public static void makeBackwardsCreation() {
        double volume = params[0];
        short[] frontwards = getAudioData();
        short[] backwards = new short[frontwards.length];
        int size = frontwards.length;
        for (int i = 0; i < size; i++) {
            backwards[i] = (short) (volume*frontwards[size - i - 1]);
        }
        closeFileOutputStream(backwards);
    }
    public static void makePhaserCreation() {
        double frequency = params[0];
        double carrier_amplitude = params[1];
        double modulator_amplitude = params[2];
        double theta = params[3];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.sin(1, frequency,theta, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
        }
        closeFileOutputStream(result);
    }
    public static void makePhaserTriangleCreation() {
        double frequency = params[0];
        double carrier_amplitude = params[1];
        double modulator_amplitude = params[2];
        double theta = params[3];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.triangle(1, frequency,theta, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
        }
        closeFileOutputStream(result);
    }
    public static void makePhaserSawCreation() {
        double frequency = params[0];
        double carrier_amplitude = params[1];
        double modulator_amplitude = params[2];
        double theta = params[3];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.saw(1, frequency,theta, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
        }
        closeFileOutputStream(result);
    }
    public static void makePhaserSquareCreation() {
        double frequency = params[0];
        double carrier_amplitude = params[1];
        double modulator_amplitude = params[2];
        double theta = params[3];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.square(1, frequency,theta, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
        }
        closeFileOutputStream(result);
    }
    public static void makeQuantizedCreation() {
        double C = params[0];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i=0; i<carrier_wave.length; i++) {
            short x = carrier_wave[i];
            if (x>0 || x<0) {
                double sample = n*(x/Math.abs(x))*C*Math.floor((Math.abs(x)/C)+.5);
                result[i]= (short) sample; }
            else {
                result[i]=x; }
        }
        closeFileOutputStream(result);
    }

    public static void makeEchoCreation() {
        double num_signals = params[0];
        double delay = params[1];
        System.out.println(delay+" and "+num_signals);
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double echo_sample = 0;
                for (int signal = 0; signal < num_signals + 1; signal++) {
                    //TODO recall limits of exponential functions. then formulate equation to scale exponent signal given delay
                    //echo_sample += .1 * carrier_wave[(int) (i - Math.pow(delay, signal))];
                    echo_sample += n * carrier_wave[(int) (i - Math.pow(delay, signal))];
                    //echo_sample += .1 * carrier_wave[(int) (i - Math.pow(delay, signal))];

                }
                result[i] = (short) echo_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (n * carrier_wave[i]);
            }
        }
        closeFileOutputStream(result);
    }

    public static void makeFlangerCreation() {
        double min = params[0];
        double max = params[1];
        double frequency = params[2];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double flanger_sample = n * carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * Math.sin(frequency * i) + .5) + min)];
                result[i] = (short) flanger_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (n * carrier_wave[i]);
            }

        }
        closeFileOutputStream(result);
    }

    public static void makeFlangerSquareCreation() {
        double min = params[0];
        double max = params[1];
        double frequency = params[2];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double yx=Generate.square_t(frequency * i);
                double flanger_sample = n * carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * yx + .5) + min)];
                result[i] = (short) flanger_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (n * carrier_wave[i]);
            }

        }
        closeFileOutputStream(result);
    }
    public static void makeFlangerTriangleCreation() {
        double min = params[0];
        double max = params[1];
        double frequency = params[2];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double yx=Generate.triangle_t(frequency * i);
                double flanger_sample = n * carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * yx + .5) + min)];
                result[i] = (short) flanger_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (n * carrier_wave[i]);
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
    public void makeAMCreation() {
        double amplitude = params[0];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) (amplitude*carrier_wave[i]);
        }
        closeFileOutputStream(result);
    }
    public void makeSRCreation() {
        //TODO make sample and playback rate not final in AudioFile
    }
}

