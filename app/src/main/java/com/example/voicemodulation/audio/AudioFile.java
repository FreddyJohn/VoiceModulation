package com.example.voicemodulation.audio;

import android.media.AudioFormat;
import android.os.Parcel;
import android.os.Parcelable;

public final class AudioFile implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AudioFile> CREATOR = new Parcelable.Creator<AudioFile>() {
        @Override
        public AudioFile createFromParcel(Parcel in) {
            return new AudioFile(in);
        }

        @Override
        public AudioFile[] newArray(int size) {
            return new AudioFile[size];
        }
    };
    private final int sample_rate;
    private final int playback_rate;
    private final int bit_depth;
    private final int num_channels_in;
    private final int num_channels_out;
    private String file_path;

    public AudioFile(int sample_rate, int playback_rate, int bit_depth, int num_channels_in, int num_channels_out) {

        this.sample_rate = sample_rate;
        this.playback_rate = playback_rate;
        this.bit_depth = bit_depth;
        this.num_channels_in = AudioFormat.CHANNEL_IN_MONO;
        this.num_channels_out = AudioFormat.CHANNEL_OUT_MONO;
    }

    protected AudioFile(Parcel in) {
        sample_rate = in.readInt();
        playback_rate = in.readInt();
        bit_depth = in.readInt();
        num_channels_in = in.readInt();
        num_channels_out = in.readInt();
        file_path = in.readString();
    }

    public int getSampleRate() {
        return sample_rate;
    }

    public int getPlaybackRate() {
        return playback_rate;
    }

    public int getBitDepth() {
        return bit_depth;
    }

    public int getNumChannelsIn() {
        return num_channels_in;
    }

    public int getNumChannelsOut() {
        return num_channels_out;
    }

    public String getFilePath() {
        return file_path;
    }

    public void setFilePath(String filePath) {
        this.file_path = filePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sample_rate);
        dest.writeInt(playback_rate);
        dest.writeInt(bit_depth);
        dest.writeInt(num_channels_in);
        dest.writeInt(num_channels_out);
        dest.writeString(file_path);
    }
}

