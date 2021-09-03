package com.example.voicemodulation.signal;
import android.util.Pair;

import com.example.voicemodulation.audio.AudioConnect;
//import com.example.voicemodulation.project.AudioData;
import com.example.voicemodulation.database.project.Project;
import com.example.voicemodulation.audio.Generate;
import com.example.voicemodulation.structures.Structure;
import com.example.voicemodulation.structures.stack.Ring;
import com.example.voicemodulation.util.Convert;

import java.io.FileOutputStream;
import java.util.LinkedList;

/*
TODO
    do not use byte[] this class should not know what a chunk is.
    instead it should use streams.
 */
public class Modulation {
    private static  double n;
    public interface modulation{
        void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable);
    }
    public static void writeToFile(short[] result, Project data){
        Convert.shortsToBytes(result);
        AudioConnect.IO_F con = new AudioConnect.IO_F();
        FileOutputStream out = con.setFileOutputStream(data.paths.modulation);
        con.closeFileOutputStream(out,Convert.shortsToBytes(result));
    }
    public static short[] readFromFile(Pair<Integer,Integer> position, Structure pieceTable){
        return Convert.bytesToShorts(pieceTable.find(position.first,position.second-position.first));
    }
    public static class backwards implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double volume = _params.get(0);
            short[] frontwards = readFromFile(position,pieceTable);
            //short[] frontwards = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] backwards = new short[frontwards.length];
            int size = frontwards.length;
            for (int i = 0; i < size; i++) {
                backwards[i] = (short) (volume*frontwards[size - i - 1]);
            }
            writeToFile(backwards,data);
        }
    }
    //TODO acting strange do something about it, review old python code for echo



    public static class echo implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double num_signals = _params.get(0);
            double delay = _params.get(1);
            short[] carrier_wave = readFromFile(position,pieceTable);
            double[] result = new double[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                double echo_sample = 0;
                double maximumDelay = i - Math.pow(delay,num_signals);
                if(maximumDelay>0 & maximumDelay<carrier_wave.length) {
                    for (int signal = 0; signal < num_signals; signal++) {
                        echo_sample += carrier_wave[(int) (i - Math.pow(delay, signal))];
                    }
                }
                else{
                    echo_sample = carrier_wave[i];
                }
                result[i] = echo_sample;
            }
            double norm = 32767.0/Generate.getAbsoluteMax(result);
            for(int i = 0; i < result.length; i++){
                carrier_wave[i] = (short) (norm*result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class quantized implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double C = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable);
            double[] result = new double[carrier_wave.length];
            for (int i=0; i<carrier_wave.length; i++) {
                double x = carrier_wave[i];
                if (x>0 || x<0) {
                    double sample = (x/Math.abs(x))*C*Math.floor((Math.abs(x)/C)+.5);
                    result[i]= sample; }
                else {
                    result[i]=x;
                }
            }
            double norm=32767.0/Generate.getAbsoluteMax(result);
            for(int i=0; i<result.length; i++){
                carrier_wave[i]= (short) (norm*result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class phaser implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
                double frequency = _params.get(0);
                double carrier_amplitude = _params.get(1);
                double modulator_amplitude = _params.get(2);
                double theta = _params.get(3);
                short[] carrier_wave = readFromFile(position,pieceTable);
                double[] modulation_wave = Generate.sin(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
                short[] result = new short[carrier_wave.length];
                for (int i = 0; i < carrier_wave.length; i++) {
                   result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
                }
                writeToFile(result,data);
        }
    }
    public static class phaserTriangle implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            short[] carrier_wave = readFromFile(position,pieceTable);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            double[] modulation_wave = Generate.triangle(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }
    public static class phaserSaw implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            short[] carrier_wave = readFromFile(position,pieceTable);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            double[] modulation_wave = Generate.saw(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }
    public static class phaserSquare implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
            double[] modulation_wave = Generate.square(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }

    public static class flanger implements modulation{

        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double ampl = 2 * data.audioData.sample_rate / 1000;
            double delay = Math.max(10*data.audioData.sample_rate/1000,ampl);
            double freq = 2 * Math.PI * 10  / data.audioData.sample_rate;
            double wet = .5;
            int bufsiz = (int) (delay+ampl)+1;
            Ring buf = new Ring(bufsiz);
            short[] carrier_wave = readFromFile(position,pieceTable);
            double[] result = new double[carrier_wave.length];
            for(int i=0; i<carrier_wave.length; i++){
                buf.enqueue(carrier_wave[i]);
                double y;
                if (i<bufsiz-1){
                    y = carrier_wave[i];
                }else{
                    double lb = delay + ampl * Math.sin(i*freq);
                    y = buf.loopback((int)lb);
                    buf.dequeue();
                }
                result[i] = (1-wet) * carrier_wave[i] + wet*y;
            }
            double norm=32767.0/Generate.getAbsoluteMax(result);
            for(int i=0; i<result.length; i++){
                carrier_wave[i]= (short) (norm*result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class flangerTriangle implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double min = _params.get(0);
            double max = _params.get(1);
            double frequency = _params.get(2);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                double yx=Generate.triangle_t(frequency * i);
                try {
                    double flanger_sample = carrier_wave[i] + carrier_wave[i - (int) ((max - min) * (.5 * yx + .5) + min)];
                    result[i] = (short) flanger_sample;
                } catch (IndexOutOfBoundsException e) {
                    result[i] = (short) (carrier_wave[i] +yx);
                }
            }
            writeToFile(result,data);
        }
    }
    public static class flangerSquare implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
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
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double smooth = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable);
            short[] result = new short[carrier_wave.length];
            double value = carrier_wave[0];
            for (int i = 0; i < carrier_wave.length-1; i++) {
                double currentValue = carrier_wave[i];
                value +=(currentValue - value) /smooth;
                result[i]= (short) value;
            }
            double norm = 32767.0/Generate.getAbsoluteMax(result);
            for(int i = 0; i < result.length; i++){
                carrier_wave[i] = (short) (norm * result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class variableEcho implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double num_signals = _params.get(0);
            double frequency = _params.get(1);
            short[] carrier_wave = readFromFile(position,pieceTable);
            double[] result = new double[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                double echo_sample = 0;
                double base = Math.sin(frequency * i);
                double maximumDelay = i - Math.pow(base,num_signals);
                if(maximumDelay>0 & maximumDelay<carrier_wave.length) {
                    for (int signal = 0; signal < num_signals; signal++) {
                        echo_sample += carrier_wave[(int) (i - Math.pow(base,signal))];
                    }
                }  else{
                    echo_sample = carrier_wave[i];
                }
                result[i] =  echo_sample;
            }
            double norm = 32767.0/Generate.getAbsoluteMax(result);
            for(int i = 0; i < result.length; i++){
                carrier_wave[i] = (short) (norm * result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class amplitude implements modulation{
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable) {
            double amplitude = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable);
            double norm = 32767.0/Generate.getAbsoluteMax(carrier_wave);
            double norm_amp = norm/amplitude;
            for (int i = 0; i < carrier_wave.length; i++) {
                carrier_wave[i]= (short) (norm*carrier_wave[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }

}

