package com.example.voicemodulation;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.widgets.ParamLogic;
import com.example.voicemodulation.audio.RecordLogic;


import java.io.IOException;

//TODO add threads for every modulation so UI does not stall
//TODO thought.... the onClick n params has a life cycle consider using ViewGroup or research Fragments
//TODO change to whatever mode that prevents user from flipping screen
//TODO add double tap or some implementation to clear, remove, and create new recording
//for example I have to add and create onClick of i modulation then remove and destroy listeners and n views.
public class ModulateActivity extends AppCompatActivity implements View.OnClickListener {
    private ModulateLogic modulate;
    private RecordLogic player;
    private LinearLayout params;
    private ParamLogic param;
    //private test t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);
        AudioFile creation = getIntent().getParcelableExtra("AudioFile");
        modulate = new ModulateLogic(creation.getPlaybackRate(), creation.getBitDepth(), creation.getFilePath());
        creation.setFilePath("/sdcard/Music/test.pcm");
        param = new ParamLogic(this,null); //(this, null);
        params = findViewById(R.id.param);
        player = new RecordLogic();
        player.setFileObject(creation);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backwards:
                try {
                    modulate.makeBackwardsCreation();
                    //ParamLogic.getParams()
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.echo:
                    params.removeAllViews();
                    ParamLogic d = new ParamLogic(this,null);
                    d.setParam("Delay",10);
                    ParamLogic ns = new ParamLogic(this,null);
                    ns.setParam("Signals",10);
                    params.addView(d);
                    params.addView(ns);
                    int delay=d.getProgress();
                    int num_signals =d.getProgress();
                    //while (d.getStatus()==false && ns.getStatus()==false){
                    //    delay= d.getProgress();
                     //   num_signals = ns.getProgress();
                    //}
                    modulate.makeEchoCreation(delay, num_signals);
                    //player.play_recording();
                break;
            case R.id.one_sample_delay:
                ParamLogic a = new ParamLogic(this,null);
                params.addView(a);
                /*
                try {
                    modulate.makeRoboticCreation();
                    ;
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

                 */
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
            case R.id.play_recording:
                try {
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }
}
