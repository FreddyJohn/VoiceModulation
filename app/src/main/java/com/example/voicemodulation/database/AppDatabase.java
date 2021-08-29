package com.example.voicemodulation.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.voicemodulation.database.project.Project;


@Database(entities = {Project.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProjectDao projectDao();
}

