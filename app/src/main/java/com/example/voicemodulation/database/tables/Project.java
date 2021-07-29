package com.example.voicemodulation.database.tables;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Project {
    @PrimaryKey(autoGenerate = true) public int uid;

    public String project_name;

    @Embedded
    public AudioData audioData;

    @Embedded
    public Paths paths;



}




