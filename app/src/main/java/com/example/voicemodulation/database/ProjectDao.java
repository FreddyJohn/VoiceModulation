package com.example.voicemodulation.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.voicemodulation.database.project.AudioData;
import com.example.voicemodulation.database.project.Paths;
import com.example.voicemodulation.database.project.Project;

import java.util.List;


@Dao
public abstract class ProjectDao{

    @Insert
    abstract void _insertProject(Project... project);

    @Query("SELECT * FROM Project WHERE project_name=:project_name")
    abstract Project getProject(String project_name);

    @Query("SELECT project_name FROM project")
    abstract List<String> _getProjectNames();

    @Update
    abstract void _updateProject(Project project);

    @Query("UPDATE Project SET project_name=:new_name WHERE uid=:uid")
    abstract void _updateProjectName(String new_name,int uid);

    @Query("UPDATE Project SET buffer_size=:buffer_size WHERE project_name=:project_name")
    abstract void _updateBufferSize(String project_name,int buffer_size);

    public int getMax(String project_name){
        return getProject(project_name).audioData.max;
    }

    public Paths getPaths(String project_name){
        return getProject(project_name).paths;
    }

    public void insertBufferSize(Project project, int buffer_size){
        _updateBufferSize(project.project_name,buffer_size);
    }
    public Project getProjectFromName(String project_name){
        return getProject(project_name);
    }
    public AudioData getAudioData(String project_name){
        return getProject(project_name).audioData;
    }
    public List<String> getProjectNames(){
          return _getProjectNames();
    }

    public void insertProject(Project project){
        _insertProject(project);
    }

}
