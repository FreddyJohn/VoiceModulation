package com.example.voicemodulation.structures.sequence;

import java.io.Serializable;

public class Piece implements Serializable{
    public boolean in_added;
    public long offset;
    public long length;
    public Piece(boolean in_added, long offset, long length){
        this.in_added=in_added;
        this.offset=offset;
        this.length=length;
    }
}
