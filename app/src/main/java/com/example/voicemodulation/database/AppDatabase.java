package com.example.voicemodulation.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.voicemodulation.database.project.Project;

//TODO define the schema location in build.gradle and find out
//  wtf schema location is
//TODO am I defining my entities correctly? looks right
@Database(entities = {Project.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProjectDao projectDao();
}

