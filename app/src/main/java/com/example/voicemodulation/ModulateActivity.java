package com.example.voicemodulation;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.controls.NControls;

//TODO add threads for every modulation so UI does not stall

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
                double scale = .01;
                closeFragment();
                displayFragment(backwards_titles,backwards_maxes,scale,null,"makeBackwardsCreation");
                break;
            case R.id.echo:
                String[] echo_titles = new String[] {"Signals","Delay"};
                int[] echo_maxes = new int[] {10,10};
                closeFragment();
                displayFragment(echo_titles,echo_maxes,1,null,"makeEchoCreation");
                break;
            case R.id.quantize:
                String[] robotic_titles = new String[] {"Quantization"};
                int[] robotic_maxes = new int[] {10};
                closeFragment();
                displayFragment(robotic_titles,robotic_maxes,1000,null,"makeQuantizedCreation");
                break;
            case R.id.phaser:
                String[] phaser_titles = new String[] {"Frequency"};
                int[] phaser_maxes = new int[] {10};
                closeFragment();
                displayFragment(phaser_titles,phaser_maxes,50,new String[]{"Hz"},"makePhaserCreation");
                break;

            case R.id.phaser_triangle:
                String[] alien_titles = new String[] {"Frequency"};
                int[] alien_maxes= new int[] {10};
                closeFragment();
                displayFragment(alien_titles,alien_maxes,50,new String[]{"Hz"},"makeAlienCreation");
                break;
            case R.id.phaser_saw:

                String[] saw_titles = new String[] {"Frequency"};
                int[] saw_maxes = new int[] {300};
                closeFragment();
                displayFragment(saw_titles,saw_maxes,50, new String[]{"Hz"},"makeRoboticCreation");
                break;
            case R.id.flanger:
                String[] flanger_titles = new String[] {"Min","Max","Frequency"};
                int[] flanger_maxes = new int[] {100,100,100};
                closeFragment();
                displayFragment(flanger_titles,flanger_maxes,50, new String[]{null,null,"Hz"},"makeFlangerCreation");
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
    public NControls displayFragment(String[] titles,int[] maxes,double scale, String[] quantity_type, String method) {
            NControls controls = NControls.newInstance(titles,maxes,scale,quantity_type,creation,method,file);
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

