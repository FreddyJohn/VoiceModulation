package com.adams.voicemodulation.audio;

import com.adams.voicemodulation.database.project.Project;
import com.adams.voicemodulation.structures.Structure;

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
