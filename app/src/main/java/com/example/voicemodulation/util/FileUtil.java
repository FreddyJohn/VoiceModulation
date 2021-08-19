package com.example.voicemodulation.util;

import android.content.Context;

import com.example.voicemodulation.database.project.Paths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {


    public static Paths createNewProjectPaths(Context context, List<String> nFiles){
        File parentDir = context.getExternalFilesDir("Projects");
        String uniqueDir = String.valueOf(System.nanoTime());
        File projectDir = new File(parentDir.getAbsolutePath()+"/"+ uniqueDir);
        projectDir.mkdirs();

        List<String> list = new ArrayList<>();
        for(String file_name: nFiles){
            File file = new File(projectDir.getAbsolutePath()+"/"+file_name);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            list.add(file.getAbsolutePath());
        }
        list.add(uniqueDir);
        return initializePaths(list);
    }
    private static Paths initializePaths(List<String> list){
        Paths projectPaths = new Paths();
        projectPaths.bitmap = list.get(0);
        projectPaths.bitmap_table = list.get(1);
        projectPaths.audio_table = list.get(2);
        projectPaths.audio_original = list.get(3);
        projectPaths.bitmap_original = list.get(4);
        projectPaths.audio = list.get(5);
        projectPaths.modulation = list.get(6);
        projectPaths.bitmap_edits = list.get(7);
        projectPaths.audio_edits = list.get(8);
        projectPaths.uniqueDir = list.get(9);
        return projectPaths;
    }
}
