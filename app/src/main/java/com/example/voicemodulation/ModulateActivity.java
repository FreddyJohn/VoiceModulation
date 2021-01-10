package com.example.voicemodulation;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;

import java.io.IOException;

public class ModulateActivity extends AppCompatActivity implements View.OnClickListener {
    private ModulateLogic modulate;
    private RecordLogic player;

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backwards:
                try {
                    modulate.makeBackwardsCreation();
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button:
                try {
                    modulate.makeEchoCreation(10, 4);
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.one_sample_delay:
                try {
                    modulate.makeRoboticCreation();
                    ;
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.phaser:
                try {
                    modulate.makePhaserCreation(20);
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.flanger:
                try {
                    modulate.makeFlangerCreation(100, 50, 20);
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.squared:
                try {
                    modulate.makeSquaredCreation();
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
