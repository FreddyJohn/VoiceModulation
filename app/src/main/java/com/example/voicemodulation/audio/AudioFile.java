package com.example.voicemodulation.audio;
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
    private final String format;

    public AudioFile(int sample_rate, int playback_rate, int bit_depth, int[] channels, String _format) {
        this.sample_rate = sample_rate;
        this.playback_rate = playback_rate;
        this.bit_depth = bit_depth;
        this.format=_format;
        this.num_channels_in= channels[0];
        this.num_channels_out =channels[1]; }
    protected AudioFile(Parcel in) {
        sample_rate = in.readInt();
        playback_rate = in.readInt();
        bit_depth = in.readInt();
        num_channels_in = in.readInt();
        num_channels_out = in.readInt();
        file_path = in.readString();
        format = in.readString();    }
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
        dest.writeString(format);
    }
    /*
    public void setFormat(Runnable _format){
        this.format=_format;
    }
     */
    public void save()
    {
        switch (format)
        {
            case ".wav":
                new Format.wav(this).run();
                break;
        }
        //format.run();
    }

}

