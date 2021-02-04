package com.example.voicemodulation;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.controls.NControls;

public class ModulateActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioFile creation;
    private String file;
    private final String[] phaser_titles = new String[] {"Frequency","Carrier Amp","Modulator Amp","Theta"};
    private final String[] phaser_quantities = new String[] {"Hz","Amp","Amp","θ"};
    private final String[] flanger_titles = new String[] {"Min","Max","Frequency"};
    private final String[] echo_titles = new String[] {"Signals","Delay"};
    private int[] phaser_progress;
    private int[] flanger_progress;
    private ImageButton stop_or_stack;
    private double nyquist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);
        stop_or_stack = findViewById(R.id.stop_recording);
        creation = getIntent().getParcelableExtra("AudioFile");
        //TODO make getNewProjectFile and getCurrentFile
        file = creation.getNewRecordFile();
        nyquist = (creation.getSampleRate()/2)/20;
        phaser_progress = new int[] {1,10,10,0};
        flanger_progress = new int[] {8,4,1};
        displayFragment(echo_titles,new int[] {10,10},new double[]{1,1},
                new String[] {"S","D"},"makeEchoCreation",Gravity.CENTER,
                "Echo Effect",new int[] {5,6});
        stop_or_stack.setOnClickListener(v -> {
            creation.setNewRecordFile(creation.getNewModulateFile());
            System.out.println("hello you've been clicked short time");
        });
        stop_or_stack.setOnLongClickListener(v -> {
            creation.setNewModulateFile(creation.getNewRecordFile());
            System.out.println("hello you've been clicked long time");

            return true;
        }); }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backwards:
                String[] backwards_titles = new String[] {"Volume"};
                int[] backwards_maxes = new int[] {10};
                closeFragment();
                displayFragment(backwards_titles,backwards_maxes,new double[]{.1},
                                new String[] {"Volume"},"makeBackwardsCreation",Gravity.CENTER,
                               "Backwards Effect",new int[] {10});
                break;
            case R.id.echo:
                int[] echo_maxes = new int[] {10,10};
                closeFragment();
                displayFragment(echo_titles,echo_maxes,new double[]{1,1},
                                new String[] {"S","D"},"makeEchoCreation",Gravity.CENTER,
                                "Echo Effect",new int[] {5,6});
                break;
            case R.id.quantize:
                String[] robotic_titles = new String[] {"Quantize","Amplitude"};
                int[] robotic_maxes = new int[] {10,10};
                closeFragment();
                displayFragment(robotic_titles,robotic_maxes,new double[]{1000,.1},
                                new String[] {"C","Amp"},"makeQuantizedCreation",Gravity.CENTER,
                                "Quantize Audio Sample",new int[] {5,10});
                break;
            case R.id.phaser:
                int[] phaser_maxes = new int[] {20,10,10,20};
                closeFragment();
                displayFragment(phaser_titles,phaser_maxes,new double[]{nyquist,.1,.1,.1*Math.PI},
                                phaser_quantities,"makePhaserCreation",Gravity.NO_GRAVITY,
                               "Phaser with Sine Wave", phaser_progress);
                break;
            case R.id.phaser_triangle:
                int[] alien_maxes= new int[] {20,10,10,10};
                closeFragment();
                displayFragment(phaser_titles,alien_maxes,new double[]{nyquist,.1,.1,.1*Math.PI},
                                phaser_quantities,"makePhaserTriangleCreation",Gravity.NO_GRAVITY,
                                "Phaser with Triangle Wave", phaser_progress);
                break;
            case R.id.phaser_square:
                int[] square_maxes = new int[] {20,10,10,10};
                closeFragment();
                displayFragment(phaser_titles,square_maxes,new double[]{nyquist,.1,.1,.1*Math.PI},
                                phaser_quantities,"makePhaserSquareCreation",Gravity.NO_GRAVITY,
                                "Phaser with Square Wave", phaser_progress);
                break;
            case R.id.phaser_saw:
                int[] saw_maxes = new int[] {20,10,10,10};
                closeFragment();
                displayFragment(phaser_titles,saw_maxes,new double[]{nyquist,.1,.1,.1*Math.PI},
                                phaser_quantities,"makePhaserSawCreation",Gravity.NO_GRAVITY,
                                "Phaser with Saw Wave", phaser_progress);
                break;
            case R.id.flanger:
                int[] flanger_maxes = new int[] {10,10,20};
                closeFragment();
                displayFragment(flanger_titles,flanger_maxes, new double[]{10, 10, nyquist},
                                new String[]{null,null,"Hz"},"makeFlangerCreation",Gravity.NO_GRAVITY,
                                "Flanger with Sine Wave",flanger_progress);
                break;
            case R.id.flanger_triangle:
                int[] flanger_triangle_maxes = new int[] {10,10,20};
                closeFragment();
                displayFragment(flanger_titles,flanger_triangle_maxes, new double[]{10, 10, nyquist},
                                new String[]{null,null,"Hz"},"makeFlangerTriangleCreation",Gravity.NO_GRAVITY,
                                "Flanger with Triangle Wave",flanger_progress);
                break;
            case R.id.flanger_square:
                int[] flanger_square_maxes = new int[] {10,10,20};
                closeFragment();
                displayFragment(flanger_titles,flanger_square_maxes, new double[]{10, 10, nyquist},
                                new String[]{null,null,"Hz"},"makeFlangerSquareCreation",Gravity.NO_GRAVITY,
                                "Flanger with Square Wave",flanger_progress);
                break;
                /*
            case R.id.flanger_saw:
                String[] flanger_titles = new String[] {"Min","Max","Frequency"};
                int[] flanger_maxes = new int[] {10,10,10};
                closeFragment();
                displayFragment(flanger_titles,flanger_maxes, new double[]{10, 10, 10}, new String[]{null,null,"Hz"},"makeFlangerCreation");
                break;

                 */
            case R.id.squared:
               // try {
                    //modulate.makeSquaredCreation();
              //      player.play_recording();
            //    } catch (IOException e) {
            //        e.printStackTrace();
             //   }
                break;

                }
    }
    public NControls displayFragment(String[] titles,int[] maxes,double[] scale, String[] quantity_type,
                                     String method, int gravity, String name, int[] progress) {
        NControls controls = NControls.newInstance(titles,maxes,scale,quantity_type,creation,method,gravity,name,progress);
        FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.user_controls,
                    controls).addToBackStack(null).commit();
            return controls; }
    public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        NControls simpleFragment = (NControls) fragmentManager
                .findFragmentById(R.id.user_controls);
        if (simpleFragment != null) {
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(simpleFragment).commit(); } }
}

