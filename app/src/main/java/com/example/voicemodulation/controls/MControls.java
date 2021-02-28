package com.example.voicemodulation.controls;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import com.example.voicemodulation.MainActivity;
import com.example.voicemodulation.R;
import com.example.voicemodulation.audio.AudioF;
import com.example.voicemodulation.audio.ModulateLogic;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.graph.AudioDisplay;
import java.io.IOException;
import java.util.LinkedList;

public class MControls extends LinearLayout{
    private SeekBar seek;
    private AudioDisplay display;
    private ImageButton play;
    private LinkedList<Controller> controllers;
    private String[] title;
    private int[] maxes;
    private double[] scale;
    private  String[] quantity_type;
    private AudioF creation;
    private ModulateLogic.modulation method;
    private int gravity;
    private String name;
    private int[] progress;

    public MControls(Context context) {
        super(context);
        init(context,null);
    }

    public MControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }


    public MControls(Context context, String[] title, int[] maxes, double[] scale,
                     String[] quantity_type, AudioF creation, ModulateLogic.modulation modulation,
                     int gravity, String name, int[] progress, ImageButton play, LinearLayout seek_n_load){
        super(context);
        this.title=title;
        this.maxes=maxes;
        this.scale=scale;
        this.quantity_type=quantity_type;
        this.creation=creation;
        this.method=modulation;
        this.gravity=gravity;
        this.name=name;
        this.progress=progress;
        this.play = play;
        this.display = seek_n_load.findViewById(R.id.audio_display);
        this.seek = seek_n_load.findViewById(R.id.seek);
        init(context,null);
    }
    public LinkedList<Double> getModulateParameters(){
        LinkedList<Double> parameters = new LinkedList<>();
        for (int i = 0; i <title.length ; i++) {
            parameters.add((double) controllers.get(i).getProgress()*scale[i]);
        }
        return parameters;
    }
    public void init(Context context, @Nullable AttributeSet attrs){
        controllers = new LinkedList<>();
        for (int i = 0; i <title.length ; i++) {
            Controller controller = new Controller(getContext(),null,quantity_type[i],scale[i]);
            controller.setParam(title[i],maxes[i],progress[i]);
            controllers.add(controller);
            addView(controller); }
        play.setOnClickListener(v-> new Thread(() -> {
            ((Activity)context).runOnUiThread(() -> {
                seek.setVisibility(View.GONE);
                display.setVisibility(View.VISIBLE);
                MainActivity.setDisplayStream(creation.getLength(),creation.getNewModulateFile(),true,0,Short.MAX_VALUE*2+1);
            });
            RecordLogic recordLogic = new RecordLogic();
            creation.setFilePath(creation.getNewModulateFile());
            recordLogic.setFileData(creation);
            method.modulate(getModulateParameters(),creation);
            System.out.println("NO NULL POINTER HAVE SCOPE "+seek.getProgress());
                recordLogic.play_recording(0,creation.getLength());
                ((Activity)context).runOnUiThread(() -> {
                    MainActivity.setDisplayStream(creation.getLength(),creation.getNewModulateFile(),false,0,Short.MAX_VALUE*2+1);
                    display.setVisibility(View.GONE);
                    seek.setVisibility(View.VISIBLE);
                });
        }).start());
    }
}

