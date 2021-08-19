package com.example.voicemodulation.structures.stack;

import java.io.Serializable;

public class Edit implements Serializable {
    public int length;
    public int offset;
    public String editType;
    public Edit(int length, int offset, String editType){
        this.length = length;
        this.offset = offset;
        this.editType = editType;
    }
}
