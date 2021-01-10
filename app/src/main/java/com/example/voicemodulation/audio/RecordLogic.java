package com.example.voicemodulation.audio;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.io.PipedWriter;
import java.io.RandomAccessFile;

public class RecordLogic {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private AudioRecord recorder;
    private AudioCon.IO io;
    private RandomAccessFile out;
    private Thread recordingThread;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private int buffer_size;
    private AudioFile file_data;
    private PipedWriter jay;
    private String file_path;

    public RecordLogic() {
        //this.file_path = filePath;
        //this.io = new AudioCon.IO(file_path);
        //this.out = io.getWriteObject(true);
    }

    public void setPipedWriter(PipedWriter jay) {
        this.jay = jay;
    }

    public void setFileObject(AudioFile creation) {
        this.file_data = creation;
        this.file_path = creation.getFilePath();
        this.io = new AudioCon.IO(file_path);
        System.out.println("THE FILE NAME IS: " + file_path);
        this.out = io.getWriteObject(true);

    }

    public void startRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(
                file_data.getSampleRate(), file_data.getNumChannelsIn(), file_data.getBitDepth());
        this.buffer_size = bufferSize;
        recorder = new AudioRecord(AUDIO_SOURCE,
                file_data.getSampleRate(), file_data.getNumChannelsIn(),
                file_data.getBitDepth(), bufferSize);
        recorder.startRecording();
        isRecording = true;
        //meet jay
        recordingThread = new Thread(() -> writeAudioDataToFile(), "AudioRecorder Thread");

        recordingThread.start();
    }

    public void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    public void setRecordingState(boolean state) {
        this.isPaused = state;
        this.out = io.getWriteObject(false);
        stopRecording();
    }

    public void writeAudioDataToFile() {
        byte[] sData = new byte[buffer_size]; //java style vs C declaration?
        io.setSeekToPointer(out, 0);
        while (isPaused == false && isRecording) {
            recorder.read(sData, 0, buffer_size);
            try {
                out.write(sData, 0, buffer_size);
                System.out.println(sData[sData.length - 1]);
                jay.write(sData[sData.length - 1]);
                jay.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
            //jay.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play_recording() throws IOException {
        System.out.println("YOU HAVE PRESSED PLAY. NOW I PLAY THIS FILE: " + file_path);
        AudioCon.IO io = new AudioCon.IO(file_path);
        byte[] byteData;
        File file;
        if (file_path != null) {
            file = new File(file_path);
            byteData = new byte[(int) file.length()];
            RandomAccessFile in = io.getReadObject();
            in.read(byteData);
            in.close();
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
        } else
            System.out.println("audio track is not initialised ");
    }
/*
    private byte[] generateHeader(
            long totalAudioLen, long totalDataLen, long longSampleRate, int channels,
            long byteRate) {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; //16 for PCM. 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * (RECORDER_BPP / 8)); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }

 */
}


