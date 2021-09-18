package com.example.voicemodulation.util;

import android.content.Context;
import android.util.Pair;

import com.example.voicemodulation.audio.AudioConnect;
import com.example.voicemodulation.database.project.Paths;
import com.example.voicemodulation.database.project.Project;
import com.example.voicemodulation.structures.Structure;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
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
        projectPaths.bitmap_remove_stack = list.get(9);
        projectPaths.audio_remove_stack = list.get(10);
        projectPaths.uniqueDir = list.get(11);
        return projectPaths;
    }
    public static void writeModulation(Project project,Structure structure, Pair<Integer,Integer> bytePoints){

        AudioConnect.IO_RAF audioConnect = new AudioConnect.IO_RAF(project.paths.audio);
        RandomAccessFile audioFile = audioConnect.getWriteObject();

        AudioConnect.IO_RAF funky = new AudioConnect.IO_RAF(project.paths.modulation);
        RandomAccessFile modulationFile = funky.getReadObject();

        byte[] bytes = read(modulationFile);
        structure.remove(bytePoints.first,bytePoints.second-bytePoints.first);
        write(bytes,audioFile);
        structure.add(bytePoints.second-bytePoints.first,bytePoints.first);

    }

    private static void write(byte[] read, RandomAccessFile audioFile) {
        try {
            audioFile.seek(audioFile.length());
            audioFile.write(read);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] read(RandomAccessFile modulationFile) {
        byte[] modulatedBytes = null;
        try {
            modulatedBytes = new byte[(int) modulationFile.length()];
            modulationFile.seek(0);
            modulationFile.read(modulatedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modulatedBytes;
    }
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

}

