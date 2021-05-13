package com.example.voicemodulation.audio;
import android.util.Pair;

import com.example.voicemodulation.util.Convert;
import com.example.voicemodulation.sequence.PieceTable;

import java.io.FileOutputStream;
import java.util.LinkedList;

/*
TODO
    do not use byte[] this class should not know what a chunk is.
    instead it should use streams.

 */
public class ModulateLogic {
    private static  double n;
    public interface modulation{
        void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable);
    }
    public static void writeToFile(short[] result,AudioF data){
        Convert.shortsToBytes(result);
        AudioCon.IO_F con = new AudioCon.IO_F();
        FileOutputStream out = con.setFileOutputStream(data.getNewModulateFile());
        con.closeFileOutputStream(out,Convert.shortsToBytes(result));
    }
    public static short[] readFromFile(Pair<Integer,Integer> position, PieceTable pieceTable){
        return Convert.bytesToShorts(pieceTable.find(position.first,position.second-position.first));
    }
    public static class backwards implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double volume = _params.get(0);
            short[] frontwards = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] backwards = new short[frontwards.length];
            int size = frontwards.length;
            for (int i = 0; i < size; i++) {
                backwards[i] = (short) (volume*frontwards[size - i - 1]);
            }
            writeToFile(backwards,data);
        }
    }
    public static class echo implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double num_signals = _params.get(0);
            double delay = _params.get(1);
            short[] carrier_wave = readFromFile(position,pieceTable);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                try {
                    double echo_sample = 0;
                    for (int signal = 0; signal < num_signals + 1; signal++) {
                        echo_sample += carrier_wave[(int) (i - Math.pow(delay, signal))];
                    }
                    result[i] = (short) echo_sample;
                } catch (IndexOutOfBoundsException e) {
                    result[i] =  carrier_wave[i];
                }
            }
            writeToFile(result,data);
        }
    }
    public static class quantized implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double C = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] result = new short[carrier_wave.length];
            for (int i=0; i<carrier_wave.length; i++) {
                short x = carrier_wave[i];
                if (x>0 || x<0) {
                    //TODO get normalization coefficient
                    double sample = .1*(x/Math.abs(x))*C*Math.floor((Math.abs(x)/C)+.5);
                    result[i]= (short) sample; }
                else {
                    result[i]=x; }
            }
            writeToFile(result,data);
        }
    }
    public static class phaser implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
                double frequency = _params.get(0);
                double carrier_amplitude = _params.get(1);
                double modulator_amplitude = _params.get(2);
                double theta = _params.get(3);
                short[] carrier_wave = readFromFile(position,pieceTable);
                double[] modulation_wave = Generate.sin(1, frequency,theta, carrier_wave.length,data.getSampleRate());
                short[] result = new short[carrier_wave.length];
                for (int i = 0; i < carrier_wave.length; i++) {
                   result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
                }
                writeToFile(result,data);
        }
    }
    public static class phaserTriangle implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            short[] carrier_wave = readFromFile(position,pieceTable);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            double[] modulation_wave = Generate.triangle(1, frequency,theta, carrier_wave.length,data.getSampleRate());
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }
    public static class phaserSaw implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            short[] carrier_wave = readFromFile(position,pieceTable);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            double[] modulation_wave = Generate.saw(1, frequency,theta, carrier_wave.length,data.getSampleRate());
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }
    public static class phaserSquare implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
            double[] modulation_wave = Generate.square(1, frequency,theta, carrier_wave.length,data.getSampleRate());
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }

    public static class flanger implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double min = _params.get(0);
            double max = _params.get(1);
            double frequency = _params.get(2);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                try {
                    double flanger_sample = n * carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * Math.sin(frequency * i) + .5) + min)];
                    result[i] = (short) flanger_sample;
                } catch (IndexOutOfBoundsException e) {
                    result[i] = (short) (n * carrier_wave[i]);
                }
            }
            writeToFile(result,data);
        }
    }
    public static class flangerTriangle implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double min = _params.get(0);
            double max = _params.get(1);
            double frequency = _params.get(2);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
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
            writeToFile(result,data);
        }
    }
    public static class flangerSquare implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double min = _params.get(0);
            double max = _params.get(1);
            double frequency = _params.get(2);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
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
            writeToFile(result,data);
        }
    }
    public static class lowPass implements modulation {
        @Override
        public void modulate(LinkedList<Double> _params, AudioF data, Pair<Integer,Integer> position, PieceTable pieceTable) {
            double smooth = _params.get(0);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
            short[] result = new short[carrier_wave.length];
            double value = carrier_wave[0];
            for (int i = 0; i < carrier_wave.length-1; i++) {
                double currentValue = carrier_wave[i];
                value +=(currentValue - value) /smooth;
                result[i]= (short) value;
            }
            writeToFile(result,data);
        }
    }
}

