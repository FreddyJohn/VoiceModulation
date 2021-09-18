package com.example.voicemodulation.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.voicemodulation.database.project.Project;


@Database(entities = {Project.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProjectDao projectDao();

}

