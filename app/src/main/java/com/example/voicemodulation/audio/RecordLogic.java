package com.example.voicemodulation.audio;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;

import com.example.voicemodulation.audio.util.Generate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedWriter;
import java.io.RandomAccessFile;
//TODO fix implementation of play/pause on every call to setFileObject there's call to AudioCon.getWriteObject
//TODO ^ this sets file length to zero for no reason.
public class RecordLogic {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private AudioRecord recorder;
    private AudioCon.IO_RAF ioRAF;
    private RandomAccessFile out;
    private Thread recordingThread;
    private boolean isRecording = false;
    private boolean isPaused = false;
    public int buffer_size;
    private AudioF file_data;
    private String file_path;
    //private int file_size;
    private DataOutputStream jack;

    public RecordLogic() {

        try {
            String name =Environment.getExternalStorageDirectory().getPath()+"/data.0";
            File i = new File(name);
            this.jack = new DataOutputStream(new FileOutputStream(name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void setFileData(AudioF file)
    {
        this.file_data = file;
        this.file_path = file.getFilePath();
    }
    public void setFileObject(AudioF creation,Boolean file_state) {
        this.file_data = creation;
        this.file_path = creation.getFilePath();
        this.ioRAF = new AudioCon.IO_RAF(file_path);
        this.out = ioRAF.getWriteObject(file_state); //TODO instead of new file or not seekPos
    }
    public void setRecordingState(boolean state) {
        this.isPaused = state;
        this.out = ioRAF.getWriteObject(false);
        stopRecording();
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
        recordingThread = new Thread(() -> writeAudioDataToFile(), "AudioRecorder Thread");
        recordingThread.setPriority(10);
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

    public void writeAudioDataToFile() {
        byte[] sData = new byte[buffer_size];
        while (!isPaused && isRecording) {
            recorder.read(sData, 0, buffer_size);
            //file_size+=sData.length/2;
            try {
                out.write(sData, 0, buffer_size);
                //jack.write(sData);
                //jack.flush();
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

    public void play_recording() throws IOException {
        AudioCon.IO_RAF ioRAF = new AudioCon.IO_RAF(file_path);
        byte[] byteData;
        File file;
        if (file_path != null) {
            file = new File(file_path);
            byteData = new byte[(int) file.length()];
            RandomAccessFile in = ioRAF.getReadObject();
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
        }
    }
}


