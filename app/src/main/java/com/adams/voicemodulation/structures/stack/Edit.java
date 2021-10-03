package com.adams.voicemodulation.structures.stack;

import java.io.Serializable;

public class Edit implements Serializable {
    public int length;
    public int offset;
    public String editType;
    public boolean in_added;
    public Edit(int length, int offset, String editType, boolean in_added){
        this.length = length;
        this.offset = offset;
        this.editType = editType;
        this.in_added = in_added;
    }
}
