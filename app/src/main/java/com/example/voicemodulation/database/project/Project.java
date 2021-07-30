package com.example.voicemodulation.database.project;

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




