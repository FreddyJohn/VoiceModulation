package com.example.voicemodulation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;


public class ParameterizeActivity extends AppCompatActivity {
    private ModulateLogic modulate;
    private RecordLogic player;

    //TODO add overloaded constructors for every time domain modulation, the idea is to have an object with n sliders that have respective x-y ranges
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);
        AudioFile creation = getIntent().getParcelableExtra("AudioFile");
        modulate = new ModulateLogic(creation.getPlaybackRate(), creation.getBitDepth(), creation.getFilePath());
        creation.setFilePath("/sdcard/Music/test.pcm");
        player = new RecordLogic();
        player.setFileObject(creation);
    }

}
