package com.adams.voicemodulation.signal;
import android.util.Pair;
import com.adams.voicemodulation.audio.AudioConnect;
import com.adams.voicemodulation.database.project.Project;
import com.adams.voicemodulation.audio.Generate;
import com.adams.voicemodulation.structures.Structure;
import com.adams.voicemodulation.structures.stack.Ring;
import com.adams.voicemodulation.util.Convert;
import java.io.FileOutputStream;
import java.util.LinkedList;

public class Modulation {
    public interface effect {
        void modulate(LinkedList<Double> _params, Project data,
                      Pair<Integer,Integer> position,
                      Structure pieceTable, String in);
    }
    public static void writeToFile(short[] result, Project data){
        Convert.shortsToBytes(result);
        AudioConnect.IO_F con = new AudioConnect.IO_F();
        FileOutputStream out = con.setFileOutputStream(data.paths.modulation);
        con.closeFileOutputStream(out,Convert.shortsToBytes(result));
    }
    public static short[] readFromFile(Pair<Integer,Integer> position, Structure pieceTable, String inPath){
        short[] audioData;
        if (inPath!=null){
            audioData = AudioConnect.Data.getShorts(inPath);
        }else{
            audioData = Convert.bytesToShorts(pieceTable.find(position.first,position.second-position.first));
        }
        return audioData;
    }
    public static class backwards implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double amplitude = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double max = Generate.getAbsoluteMax(carrier_wave);
            double norm = 32767.0/max;
            double coefficient = amplitude * norm;
            int size = carrier_wave.length;
            for (int i = 0; i < size; i++) {
                carrier_wave[i] = (short) (coefficient * carrier_wave[size - i - 1]);
            }
            writeToFile(carrier_wave,data);
        }
    }

    public static class echo implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double delay = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double[] result = new double[carrier_wave.length];
            int output_delay = (int) (delay*data.audioData.sample_rate);
            for (int i = 0; i < carrier_wave.length; i++) {
                if (i < output_delay){
                    result[i]=carrier_wave[i];
                }else {
                    result[i] = carrier_wave[i] + carrier_wave[i-output_delay];
                }
            }
            double norm = 32767.0/Generate.getAbsoluteMax(result);
            for(int i = 0; i < result.length; i++){
                carrier_wave[i] = (short) (norm*result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class quantized implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double C = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
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
    public static class phaser implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
                double frequency = _params.get(0);
                double amplitude = _params.get(1);
                double theta = _params.get(2);
                short[] carrier_wave = readFromFile(position,pieceTable,in);
                double max = Generate.getAbsoluteMax(carrier_wave);
                double norm = 32767.0/max;
                double coefficient = amplitude * norm;
                double[] modulation_wave = Generate.sin(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
                short[] result = new short[carrier_wave.length];
                for (int i = 0; i < carrier_wave.length; i++) {
                  // result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
                  result[i] = (short) (coefficient* (carrier_wave[i] * modulation_wave[i]));

                }
                writeToFile(result,data);
        }
    }
    public static class phaserTriangle implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            double[] modulation_wave = Generate.triangle(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }
    public static class phaserSaw implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            double[] modulation_wave = Generate.saw(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }
    public static class phaserSquare implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double frequency = _params.get(0);
            double carrier_amplitude = _params.get(1);
            double modulator_amplitude = _params.get(2);
            double theta = _params.get(3);
            //short[] carrier_wave = AudioCon.Data.getShorts(data.getNewRecordFile());
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double[] modulation_wave = Generate.square(1, frequency,theta, carrier_wave.length,data.audioData.sample_rate);
            short[] result = new short[carrier_wave.length];
            for (int i = 0; i < carrier_wave.length; i++) {
                result[i] = (short) ((carrier_amplitude*carrier_wave[i])*(modulator_amplitude*modulation_wave[i]));
            }
            writeToFile(result,data);
        }
    }

    public static class flanger implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double delayms = _params.get(0);
            double ampl = _params.get(1) * data.audioData.sample_rate / 1000;
            double wet = _params.get(2);
            double freq = _params.get(3) * Math.PI * 10  / data.audioData.sample_rate;
            double delay = Math.max(delayms*data.audioData.sample_rate/1000,ampl);
            int bufsiz = (int) (delay+ampl)+1;
            Ring buf = new Ring(bufsiz);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
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
                carrier_wave[i] = (short) (norm * result[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }
    public static class flangerTriangle implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double delayms = _params.get(0);
            double ampl = _params.get(1) * data.audioData.sample_rate / 1000;
            double wet = _params.get(2);
            double freq = _params.get(3) * Math.PI * 10  / data.audioData.sample_rate;
            double delay = Math.max(delayms*data.audioData.sample_rate/1000,ampl);
            int bufsiz = (int) (delay+ampl)+1;
            Ring buf = new Ring(bufsiz);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double[] result = new double[carrier_wave.length];
            for(int i=0; i<carrier_wave.length; i++){
                buf.enqueue(carrier_wave[i]);
                double y;
                if (i<bufsiz-1){
                    y = carrier_wave[i];
                }else{
                    double lb = delay + ampl * Generate.triangle_t(freq*i);
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
    public static class flangerSquare implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double delayms = _params.get(0);
            double ampl = _params.get(1) * data.audioData.sample_rate / 1000;
            double wet = _params.get(2);
            double freq = _params.get(3) * Math.PI * 10  / data.audioData.sample_rate;
            double delay = Math.max(delayms*data.audioData.sample_rate/1000,ampl);
            int bufsiz = (int) (delay+ampl)+1;
            Ring buf = new Ring(bufsiz);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double[] result = new double[carrier_wave.length];
            for(int i=0; i<carrier_wave.length; i++){
                buf.enqueue(carrier_wave[i]);
                double y;
                if (i<bufsiz-1){
                    y = carrier_wave[i];
                }else{
                    double lb = delay + ampl * Generate.square_t(freq*i);
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
    public static class flangerSaw implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer, Integer> position, Structure pieceTable, String in) {
            double delayms = _params.get(0);
            double ampl = _params.get(1) * data.audioData.sample_rate / 1000;
            double wet = _params.get(2);
            double freq = _params.get(3) * Math.PI * 10  / data.audioData.sample_rate;
            double delay = Math.max(delayms*data.audioData.sample_rate/1000,ampl);
            int bufsiz = (int) (delay+ampl)+1;
            Ring buf = new Ring(bufsiz);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double[] result = new double[carrier_wave.length];
            for(int i=0; i<carrier_wave.length; i++){
                buf.enqueue(carrier_wave[i]);
                double y;
                if (i<bufsiz-1){
                    y = carrier_wave[i];
                }else{
                    double lb = delay + ampl * Generate.saw_t(freq*i);
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
    public static class lowPass implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double smooth = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
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

    public static class amplitude implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer,Integer> position, Structure pieceTable, String in) {
            double amplitude = _params.get(0);
            short[] carrier_wave = readFromFile(position,pieceTable,in);
            double max = Generate.getAbsoluteMax(carrier_wave);
            double norm = 32767.0/max;
            double coefficient = amplitude * norm;
            for (int i = 0; i < carrier_wave.length; i++) {
                carrier_wave[i]= (short) (coefficient*carrier_wave[i]);
            }
            writeToFile(carrier_wave,data);
        }
    }

    public static class robot implements effect {
        @Override
        public void modulate(LinkedList<Double> _params, Project data, Pair<Integer, Integer> position, Structure pieceTable, String in) {

            double robotness = _params.get(0);

            Modulation.echo echo = new Modulation.echo();
            echo.modulate(new LinkedList<Double>(){{add(robotness*.02);}},data,position,pieceTable,null);

            Modulation.phaser phaser = new Modulation.phaser();
            phaser.modulate(new LinkedList<Double>(){{add(robotness*1200.0); add(1.0); add(1.0); add(0.0);}},data,position,pieceTable,data.paths.modulation);

            Modulation.quantized quantized = new Modulation.quantized();
            quantized.modulate(new LinkedList<Double>(){{add(1000.0);}},data,position,pieceTable,data.paths.modulation);

            Modulation.lowPass lowPass = new Modulation.lowPass();
            lowPass.modulate(new LinkedList<Double>(){{add(150.0);}},data,position,pieceTable,data.paths.modulation);

            echo.modulate(new LinkedList<Double>(){{add(robotness*.04);}},data,position,pieceTable,data.paths.modulation);

        }
    }

}

