package com.example.voicemodulation;
import java.util.HashMap;


public final class AudioFile {
    private final int sample_rate;
    private final int playback_rate;
    private final int bit_depth;
    private final int num_channels;
    private final String file_name;

    public AudioFile(int sample_rate, int playback_rate,int bit_depth,int num_channels, String file_name) {
        this.sample_rate=sample_rate;
        this.playback_rate=playback_rate;
        this.bit_depth=bit_depth;
        this.num_channels=num_channels;
        this.file_name=file_name;
    }
}
