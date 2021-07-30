package com.example.voicemodulation.audio;

import com.example.voicemodulation.database.project.Project;
import com.example.voicemodulation.sequence.PieceTable;

public class Export {
    public static void format(Project data, PieceTable sequence){
        switch (data.audioData.format)
        {
            case ".wav":
                new Format.wav(data, sequence).run();
                break;
        }
    }
}
