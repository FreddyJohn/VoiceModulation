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
public class ModulateLogic {
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static int PLAYBACK_SAMPLE_RATE;
    private static int SELECTED_AUDIO_ENCODING;
    private static String CREATION_NAME;
    private static FileOutputStream out;
    private byte[] track;
    private static int[] params;

    public ModulateLogic(int[] _params, String s, int play_back_rate){
        params=_params;
        CREATION_NAME = s;
        PLAYBACK_SAMPLE_RATE = play_back_rate;}
    public ModulateLogic(int _PLAYBACK_SAMPLE_RATE, int _SELECTED_AUDIO_ENCODING, String _SELECTED_FILE_NAME) {
        this.SELECTED_AUDIO_ENCODING = _SELECTED_AUDIO_ENCODING;
        this.PLAYBACK_SAMPLE_RATE = _PLAYBACK_SAMPLE_RATE;
        this.CREATION_NAME = _SELECTED_FILE_NAME;
    }
    public static void setFileOutputStream(String filePath) {
        try {
            out = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void closeFileOutputStream(short[] data) {
        byte[] bytes = Convert.getBytesFromShorts(data);
        try {
            out.write(bytes, 0, bytes.length);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getBytesFromTrack() {
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

    public static short[] getAudioData() {
        setFileOutputStream("/sdcard/Music/test.pcm");
        byte[] bytes = getBytesFromTrack();
        short[] shorts = Convert.getShortsFromBytes(bytes);
        return shorts;
    }

    public static void makeBackwardsCreation() {
        int volume = params[0];
        short[] frontwards = getAudioData();
        short[] backwards = new short[frontwards.length];
        int size = frontwards.length;
        for (int i = 0; i < size; i++) {
            backwards[i] = (short) (volume*frontwards[size - i - 1]);
        }
        closeFileOutputStream(backwards);
    }

    public static void makePhaserCreation() {
        int frequency = params[0];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.sin(1, frequency, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) (carrier_wave[i] * modulation_wave[i]);
        }
        closeFileOutputStream(result);
    }
    public static void makeAlienCreation() {
        int frequency = params[0];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.triangle(1, frequency, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) (carrier_wave[i] * modulation_wave[i]);
        }
        closeFileOutputStream(result);
    }
    public static void makeRoboticCreation() {
        int frequency = params[0];
        short[] carrier_wave = getAudioData();
        double[] modulation_wave = Generate.saw(1, frequency, carrier_wave.length,PLAYBACK_SAMPLE_RATE);
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            result[i] = (short) (carrier_wave[i] * modulation_wave[i]);
        }
        closeFileOutputStream(result);
    }


    public static void makeQuantizedCreation() {
        int C = params[0];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i=0; i<carrier_wave.length; i++) {
            short x = carrier_wave[i];
            if (x>0 || x<0) {
                double sample = .1*((x/Math.abs(x))*C*Math.floor((Math.abs(x)/C)+.5));
                result[i]= (short) sample; }
            else {
                result[i]=x; }
        }
        closeFileOutputStream(result);
    }

    public static void makeEchoCreation() {
        int num_signals = params[0];
        int delay = params[1];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double echo_sample = 0;
                for (int signal = 0; signal < num_signals + 1; signal++) {
                    echo_sample += .1 * carrier_wave[(int) (i - Math.pow(delay, signal))];
                }
                result[i] = (short) echo_sample;
            } catch (IndexOutOfBoundsException e) {
                result[i] = (short) (.1 * carrier_wave[i]);
            }
        }
        closeFileOutputStream(result);
    }

    public static void makeFlangerCreation() {
        int min = params[0];
        int max = params[1];
        int frequency = params[2];
        short[] carrier_wave = getAudioData();
        short[] result = new short[carrier_wave.length];
        for (int i = 0; i < carrier_wave.length; i++) {
            try {
                double flanger_sample = .1 * carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * Math.sin(frequency * i) + .5) + min)];
                //System.out.println(flanger_sample);
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
    public void makeAMCreation() {
        int amplitude = params[0];
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

