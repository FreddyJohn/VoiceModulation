package com.example.voicemodulation;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.widgets.Controller;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.widgets.nControls;


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
    private AudioFile creation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);
        creation = getIntent().getParcelableExtra("AudioFile");
        modulate = new ModulateLogic(creation.getPlaybackRate(), creation.getBitDepth(), creation.getFilePath());
        //creation.setFilePath("/sdcard/Music/test.pcm");
        params = findViewById(R.id.n_parameters);
        player = new RecordLogic();
        //player.setFileObject(creation);
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

                String[] titles = new String[] {"Signals","Delay"};
                int[] maxes = new int[] {10,10};
                ModulateLogic.Parameters echo = (int[] params) -> ModulateLogic.makeEchoCreation(params);
                displayFragment(titles,maxes,echo);

                break;
            case R.id.one_sample_delay:
                Controller a = new Controller(this,null);
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
                String[] phaser_titles = new String[] {"Frequency"};
                int[] phaser_maxes = new int[] {40};
                ModulateLogic.Parameters phaser = (int[] params) -> ModulateLogic.makePhaserCreation(params);
                displayFragment(phaser_titles,phaser_maxes,phaser);
                /*
                try {
                    modulate.makePhaserCreation(20);
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                 */
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
    public nControls displayFragment(String[] titles, int[] maxes, ModulateLogic.Parameters echo) {
        nControls controls = nControls.newInstance(titles,maxes,echo,creation);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.god_mode,
                controls).addToBackStack(null).commit();

        return controls;
    }
    public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        nControls simpleFragment = (nControls) fragmentManager
                .findFragmentById(R.id.god_mode);
        if (simpleFragment != null) {
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(simpleFragment).commit();
        }
    }
}
