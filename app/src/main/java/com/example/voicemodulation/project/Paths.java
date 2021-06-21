package com.example.voicemodulation.project;

import java.io.Serializable;
public class Paths implements Serializable {
    public String bitmap;
    public String audio;
    public String bitmap_table;
    public String audio_table;
    public String audio_original;
    public String bitmap_original;
    public String modulation;
    public Paths(String bitmap
            , String audio
            , String bitmap_table
            , String audio_table
            , String audio_original
            , String bitmap_original
            , String modulation){
        this.audio=audio;
        this.audio_original = audio_original;
        this.audio_table = audio_table;
        this.bitmap = bitmap;
        this.bitmap_original = bitmap_original;
        this.bitmap_table = bitmap_table;
        this.modulation = modulation;
    }
}
