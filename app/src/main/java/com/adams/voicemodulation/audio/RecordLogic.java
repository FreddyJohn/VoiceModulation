package com.adams.voicemodulation.audio;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import com.adams.voicemodulation.database.project.AudioData;
import com.adams.voicemodulation.structures.Structure;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordLogic {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private AudioRecord recorder;
    private AudioConnect.IO_RAF ioRAF;
    private RandomAccessFile out;
    private Thread recordingThread;
    private boolean isRecording = false;
    private boolean isPaused = false;
    public int buffer_size;
    public long record_size;
    private AudioData audioData;
    private String file_path;
    private Structure pieceTable;
    private AudioTrack at;

    public RecordLogic() {
    }
    public void setPieceTable(Structure pieceTable){
        this.pieceTable = pieceTable;
    }
    public void setFileData(AudioData file, String path)
    {
        this.audioData = file;
        this.file_path = path;
    }
    public void setFileObject(AudioData audioData, String path) {
        this.audioData = audioData;
        //this.file_path = creation.getFilePath();
        //this.file_path=creation.projectPaths.audio;
        this.file_path = path;
        this.ioRAF = new AudioConnect.IO_RAF(file_path);
        this.buffer_size = AudioRecord.getMinBufferSize(
                this.audioData.sample_rate, this.audioData.num_channels_in, this.audioData.bit_depth);
        this.out = ioRAF.getWriteObject();
    }
    public void isPaused(boolean state) {
        this.isPaused = state;
        this.out = ioRAF.getWriteObject();
        stopRecording();
    }
    public void startRecording() {

        /*
        int bufferSize = AudioRecord.getMinBufferSize(
                file_data.getSampleRate(), file_data.getNumChannelsIn(), file_data.getBitDepth());
        this.buffer_size = bufferSize;
        System.out.println(file_data.getSampleRate()+","+file_data.getNumChannelsIn()+","+
                file_data.getBitDepth()+","+bufferSize);
        recorder = new AudioRecord(AUDIO_SOURCE,
                file_data.getSampleRate(), file_data.getNumChannelsIn(),
                file_data.getBitDepth(), bufferSize);
         */
        //int bufferSize = AudioRecord.getMinBufferSize(
        //       file_data.sample_rate, file_data.num_channels_in, file_data.bit_depth);
       // this.buffer_size = bufferSize;
        //System.out.println("buffer size as calculated in RecordLogic = "+bufferSize);
        recorder = new AudioRecord(AUDIO_SOURCE,
                audioData.sample_rate, audioData.num_channels_in,
                audioData.bit_depth, this.buffer_size);
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(this::writeAudioDataToFile, "AudioRecorder Thread");
        recordingThread.setPriority(10);
        recordingThread.start();
    }

    public void stopRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder.release();
            isRecording = false;
            recorder = null;
            recordingThread = null;
            record_size=0;
        }
    }

    public void writeAudioDataToFile() {
        byte[] sData = new byte[buffer_size];
        while (!isPaused && isRecording) {
            recorder.read(sData, 0, buffer_size);
            try {
                //record_size+=sData.length;
                out.write(sData, 0, buffer_size);
                record_size+=sData.length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void play_recording(int offset, int length) {
        AudioConnect.IO_RAF ioRAF = new AudioConnect.IO_RAF(file_path);
        byte[] byteData;
        File file;
        if (file_path != null) {
            file = new File(file_path);
            RandomAccessFile in = ioRAF.getReadObject();
            if (pieceTable == null) {
                byteData = AudioConnect.Data.getAudioChunk(offset, length - offset, 0, in);
            } else {
                byteData = pieceTable.find(offset, length - offset);
            }/*
            int intSize = android.media.AudioTrack.getMinBufferSize(
                    file_data.getPlaybackRate(), file_data.getNumChannelsOut(), file_data.getBitDepth());
            at = new AudioTrack(
                    AudioManager.STREAM_MUSIC, file_data.getPlaybackRate(), file_data.getNumChannelsOut(),
                    file_data.getBitDepth(), intSize, AudioTrack.MODE_STREAM);
                    */
            //System.out.println(file_data.playback_rate+","+file_data.num_channels_out+","+file_data.bit_depth);

            int intSize = android.media.AudioTrack.getMinBufferSize(
                    audioData.playback_rate, audioData.num_channels_out, audioData.bit_depth);
            at = new AudioTrack(
                    AudioManager.STREAM_MUSIC, audioData.playback_rate, audioData.num_channels_out,
                    audioData.bit_depth, intSize, AudioTrack.MODE_STREAM);
            if (at != null) {
                at.play();
                at.write(byteData, 0, byteData.length);
                at.stop();
                at.release();
            }
        }
    }
    public void stop_playing(){
        if (at != null ) {
           try {
               at.pause();
               at.flush();
           }catch (IllegalStateException e){
               e.printStackTrace();}
        }
    }
    /*
    public void play_recording(int offset, int length) {
        AudioConnect.IO_RAF ioRAF = new AudioConnect.IO_RAF(file_path);
        byte[] byteData;
        File file;
        if (file_path != null) {
            file = new File(file_path);
            RandomAccessFile in = ioRAF.getReadObject();
            if (pieceTable==null){
            byteData = AudioConnect.Data.getAudioChunk(offset,length-offset,0,in);}
            else{
            byteData = pieceTable.get_text();}
            int intSize = android.media.AudioTrack.getMinBufferSize(
                    file_data.getPlaybackRate(), file_data.getNumChannelsOut(), file_data.getBitDepth());
            AudioTrack at = new AudioTrack(
                    AudioManager.STREAM_MUSIC, file_data.getPlaybackRate(), file_data.getNumChannelsOut(),
                    file_data.getBitDepth(), intSize, AudioTrack.MODE_STREAM);
            if (at != null) {
                at.play();
                at.write(byteData, 0, byteData.length);
                at.stop();
                at.release();
            }
        }
    }
     */

}


