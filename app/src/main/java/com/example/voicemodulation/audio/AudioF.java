package com.example.voicemodulation.audio;

import android.media.AudioFormat;
import android.os.Environment;

public class AudioF {
    private int sample_rate;
    private int playback_rate;
    private int bit_depth;
    private int num_channels_in;
    private int num_channels_out;
    private String file_path;
    //TODO make not fixed file but project file
    private String record_file = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
    private String modulation_file = Environment.getExternalStorageDirectory().getPath()+"/mod.pcm";
    private  String format;
    public AudioF(){}

    public int getSampleRate() {
        return sample_rate;
    }
    public void setSampleRate(int rate) {
        this.sample_rate =rate;
    }
    public int getPlaybackRate() {
        return playback_rate;
    }
    public void setPlaybackRate(int rate) {
        this.playback_rate =rate;
    }
    public int getBitDepth() {
        return bit_depth;
    }
    public void setBitDepth(int depth) {
        this.bit_depth =depth;
    }
    public int getNumChannelsIn() {
        return num_channels_in;
    }
    public void setNumChannelsIn(int in) {
        this.num_channels_in=in;
        this.num_channels_out=channelSeeker(in);
        setNumChannelsOut(num_channels_out);
    }
    private void setNumChannelsOut(int out) {
        this.num_channels_out=out;
    }
    public int getNumChannelsOut() {
        return num_channels_out;
    }

    public void setFormat(String _format)
    {
        this.format = _format;
    }
    public String getFormat()
    {
        return format;
    }
    public String getFilePath() {
        return file_path;
    }
    public void setFilePath(String filePath) {
        this.file_path = filePath;
    }
    public String getNewRecordFile(){return this.record_file;}
    public String getNewModulateFile(){return this.modulation_file;}
    public void setNewModulateFile(String file_path){ this.modulation_file =file_path;}
    public void setNewRecordFile(String file_path){ this.record_file =file_path;}
    public static int channelSeeker(int num_channels_in) {
        int out=0;
        switch (num_channels_in) {
            case AudioFormat.CHANNEL_IN_STEREO:
                out = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            case AudioFormat.CHANNEL_IN_MONO:
                out = AudioFormat.CHANNEL_OUT_MONO;
                break;
        }
        return out;
    }
}
