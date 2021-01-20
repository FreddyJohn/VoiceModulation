package com.example.voicemodulation;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.controls.Controller;
import com.example.voicemodulation.controls.NControls;

//TODO add threads for every modulation so UI does not stall
//TODO change to whatever mode that prevents user from flipping screen
//TODO add double tap or some implementation to clear, remove, and create new recording
//for example I have to add and create onClick of i modulation then remove and destroy listeners and n views.
public class ModulateActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean isFragmentDisplayed = false;
    private LinearLayout params;
    private AudioFile creation;
    private String file = "/sdcard/Music/creation_test.pcm";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);
        creation = getIntent().getParcelableExtra("AudioFile");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backwards:
                String[] backwards_titles = new String[] {"Volume"};
                int[] backwards_maxes = new int[] {10};
                if (!isFragmentDisplayed) {
                    displayFragment(backwards_titles,backwards_maxes,"makeBackwardsCreation");
                } else {
                    closeFragment();
                }
                break;
            case R.id.echo:
                String[] echo_titles = new String[] {"Signals","Delay"};
                int[] echo_maxes = new int[] {10,10};
                if (!isFragmentDisplayed) {
                    displayFragment(echo_titles,echo_maxes,"makeEchoCreation");
                } else {
                    closeFragment();
                }

                break;
            case R.id.one_sample_delay:
                String[] robotic_titles = new String[] {"n Samples"};
                int[] robotic_maxes = new int[] {30};
                if (!isFragmentDisplayed) {
                    displayFragment(robotic_titles,robotic_maxes,"makeRoboticCreation");
                } else {
                    closeFragment();
                }
                break;
            case R.id.phaser:
                String[] phaser_titles = new String[] {"Frequency"};
                int[] phaser_maxes = new int[] {100};
                if (!isFragmentDisplayed) {
                    displayFragment(phaser_titles,phaser_maxes,"makePhaserCreation");
                } else {
                    closeFragment();
                }
                break;
            case R.id.flanger:
                String[] flanger_titles = new String[] {"Min","Max","Frequency"};
                int[] flanger_maxes = new int[] {100,100,100};
                if (!isFragmentDisplayed) {
                    displayFragment(flanger_titles,flanger_maxes,"makeFlangerCreation");
                } else {
                    closeFragment();
                }
                break;
            case R.id.squared:
               // try {
                    //modulate.makeSquaredCreation();
              //      player.play_recording();
            //    } catch (IOException e) {
            //        e.printStackTrace();
             //   }
                break; }
    }
    public NControls displayFragment(String[] titles,int[] maxes, String method) {
            NControls controls = NControls.newInstance(titles,maxes,creation,method,file);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.god_mode,
                    controls).addToBackStack(null).commit();
            isFragmentDisplayed = true;
            return controls; }
    public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        NControls simpleFragment = (NControls) fragmentManager
                .findFragmentById(R.id.god_mode);
        if (simpleFragment != null) {
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(simpleFragment).commit();
            isFragmentDisplayed = false; } }
}

