package com.example.voicemodulation;
import android.app.Activity;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.DisplayMetrics;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class RecordLogic {
    private AudioRecord recorder;
    private Thread recordingThread;
    private boolean isRecording = false;
    private static int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;;
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static int RECORDER_AUDIO_ENCODING;
    private static int RECORDER_SAMPLE_RATE;
    private static int PLAYBACK_SAMPLE_RATE;
    private static String filePath;
    private LineGraphSeries mSeries;
    public Activity activity;
    RecordLogic (Activity _activity)
    {
        this.activity=_activity;
        //DisplayMetrics.
    }
    public void setFilePath(String filePath)
    {
        this.filePath=filePath;
    }
    public void setAudioEncoding(int encoding)
    {
          this.RECORDER_AUDIO_ENCODING=encoding;
    }
    public void setSampleRate(int rate)
    {
        this.RECORDER_SAMPLE_RATE=rate;
    }
    public void setPlayBackRate(int rate)
    {
        this.PLAYBACK_SAMPLE_RATE=rate;
    }
    public void start_recording()
    {
       int bufferSize =AudioRecord.getMinBufferSize(
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);
        System.out.println("the selected encoding : " + RECORDER_AUDIO_ENCODING);
        System.out.println("the selected sample rate : " +RECORDER_SAMPLE_RATE);
        recorder = new AudioRecord(AUDIO_SOURCE,
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN,
                RECORDER_AUDIO_ENCODING, bufferSize);
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }
    public void stop_recording(){
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    public void writeAudioDataToFile()
    {
        float dp = Resources.getSystem().getDisplayMetrics().density;
        System.out.println("THIS IS THE SCREEN PIXEL DENSITY: "+dp);
        GraphView graph = (GraphView) this.activity.findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        int count = 0;
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1000);
        graph.getViewport().setMaxY(100*dp);
        //increment by dp
        int bufferSize = AudioRecord.getMinBufferSize(
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);
        byte sData[] = new byte[bufferSize];
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording==true) {
            recorder.read(sData,0,bufferSize);
            double p2 = sData[sData.length - 1];
            double decibel;
            count+=1*dp;
            decibel = Math.abs(20.0*Math.log10(Math.abs(p2)/65536.0));
            double decibel_scaled=decibel*(dp/2);
            if (p2!=0 && p2>0)
            {
                mSeries.appendData(new DataPoint(count, decibel_scaled), true, 400);
            }
            else
            {
                mSeries.appendData(new DataPoint(count, 0), true, 400);
            }
            try {
                os.write(sData, 0, bufferSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("count: "+ count);
            System.out.println("decibel: "+ decibel);
            System.out.println("bufferSize: "+ bufferSize);
            System.out.println("p2: "+ p2);
            System.out.println("sData: "+ Arrays.toString(sData));
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void play_recording() throws IOException
    {
        System.out.println("YOU HAVE PRESSED PLAY. NOW I PLAY THIS FILE: "+filePath);
        byte[] byteData;
        File file;
        if (filePath!=null)
        {
            file = new File(filePath);
            byteData = new byte[(int) file.length()];
            FileInputStream in;
            try {
                in = new FileInputStream(file);
                in.read(byteData);
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int intSize = android.media.AudioTrack.getMinBufferSize(
                    PLAYBACK_SAMPLE_RATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING);
            AudioTrack at = new AudioTrack(
                    AudioManager.STREAM_MUSIC, PLAYBACK_SAMPLE_RATE, RECORDER_CHANNELS_OUT,
                    RECORDER_AUDIO_ENCODING, intSize, AudioTrack.MODE_STREAM);
            if (at != null) {
                at.play();
                at.write(byteData, 0, byteData.length);
                at.stop();
                at.release();
            }
        }
        else
            System.out.println("audio track is not initialised ");
    }
}

