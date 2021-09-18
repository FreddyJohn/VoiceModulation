package com.example.voicemodulation.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.voicemodulation.database.project.Project;

import java.util.List;


@Dao
    public abstract class ProjectDao{

        @Insert
        abstract void _insertProject(Project... project);

        @Update
        abstract  void _updateProject(Project project);

        @Query("SELECT * FROM Project WHERE project_name=:project_name")
        abstract Project getProject(String project_name);

        @Query("SELECT project_name FROM project")
        abstract List<String> _getProjectNames();

        @Query("SELECT uid FROM project ORDER BY uid DESC LIMIT 1")
        abstract int _getUid();


        @Query("UPDATE Project SET project_name=:new_name WHERE uniqueDir=:uid")
        abstract void _updateProjectName(String new_name,String uid);

        @Query("DELETE FROM Project WHERE project_name=:name")
        abstract void _deleteProject(String name);

        @Query("UPDATE Project SET buffer_size=:buffer_size WHERE project_name=:project_name")
        abstract void _updateBufferSize(String project_name,int buffer_size);


        public void insertBufferSize(Project project, int buffer_size){
            _updateBufferSize(project.project_name,buffer_size);
        }
        public Project getProjectFromName(String project_name){
            return getProject(project_name);
        }

        public List<String> getProjectNames(){
            return _getProjectNames();
        }

        public void insertProject(Project project){
            _insertProject(project);
        }

        public void updateProjectName(String name, Project project){
            //project.project_name = name;
            //_deleteProject(name);
            //insertProject(project);
            _updateProjectName(name, project.paths.uniqueDir);
            //_updateProject(project);
        }

        public void deleteProject(String name){
            System.out.println(getProject(name).paths.uniqueDir);
            _deleteProject(name);
        }


        public int getUid(){
            return _getUid();
        }
}
