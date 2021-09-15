package com.example.voicemodulation.audio;

import com.example.voicemodulation.database.project.Project;
import com.example.voicemodulation.structures.Structure;

public class Export {
    public static void format(Project data, Structure sequence, String path){
        switch (data.audioData.format)
        {
            case ".wav":
                new Format.wav(data, sequence,path).run();
                break;
        }
    }
}
