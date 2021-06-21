package com.example.voicemodulation.controls;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.voicemodulation.MainActivity;
import com.example.voicemodulation.R;
import com.example.voicemodulation.project.AudioData;
import com.example.voicemodulation.modulate.TimeDomain;
import com.example.voicemodulation.sequence.PieceTable;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.graph.AudioDisplay;
import java.util.LinkedList;

public class ModulateControls extends LinearLayout{
    private PieceTable pieceTable;
    private SeekBar seek;
    private AudioDisplay display;
    private ImageButton play;
    private LinkedList<Controller> controllers;
    private String[] title;
    private int[] maxes;
    private double[] scale;
    private  String[] quantity_type;
    private AudioData creation;
    private TimeDomain.modulation method;
    private int gravity;
    private String name;
    private int[] progress;
    private Pair<Integer,Integer> position;

    public ModulateControls(Context context) {
        super(context);
        init(context,null);
    }

    public ModulateControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }


    public ModulateControls(Context context, String[] title, int[] maxes, double[] scale,
                            String[] quantity_type, AudioData creation, TimeDomain.modulation modulation,
                            int gravity, String name, int[] progress, ImageButton play,
                            LinearLayout seek_n_load, PieceTable pieceTable){
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
        this.pieceTable = pieceTable;
        init(context,null);
    }
    //TODO we keep running into this problem.
    // see now we want to have variable control over not only numerical types but also operation types such as +,-,/,*
    // because these are different types we cannot use a LinkedList, Pair, HashMap, etc
    // we can create an object
    public LinkedList<Double> getModulateParameters(){
        LinkedList<Double> parameters = new LinkedList<>();
        for (int i = 0; i <title.length ; i++) {
            parameters.add((double) controllers.get(i).getProgress()*scale[i]);
        }
        return parameters;
    }

    public void init(Context context, @Nullable AttributeSet attrs){
        controllers = new LinkedList<>();
        TextView control_title = ((Activity)context).findViewById(R.id.control_title);
        control_title.setText(name);
        //setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //setOrientation(HORIZONTAL);
        for (int i = 0; i <title.length ; i++) {
            Controller controller = new Controller(getContext(),null,quantity_type[i],scale[i]);
            controller.setParam(title[i],maxes[i],progress[i]);
            controllers.add(controller);
            addView(controller); }
        play.setOnClickListener(v-> new Thread(() -> {
            ((Activity)context).runOnUiThread(() -> {
                seek.setVisibility(View.GONE);
                display.setVisibility(View.VISIBLE);
                MainActivity.setDisplayStream(creation.getLength(),creation.projectPaths.modulation,true,0,Short.MAX_VALUE*2+1);
            });
            position = MainActivity.getSelectionPoints();
            RecordLogic recordLogic = new RecordLogic();
            recordLogic.setPieceTable(null);
            //creation.setFilePath(creation.getNewModulateFile());
            //recordLogic.setFileData(creation);
            recordLogic.setFileData(creation,creation.projectPaths.modulation);
            method.modulate(getModulateParameters(),creation, position,pieceTable);
            //System.out.println("NO NULL POINTER HAVE SCOPE "+seek.getProgress());
                //recordLogic.play_recording(0,creation.getLength());
                recordLogic.play_recording(0,position.second-position.first);
                System.out.println("WE WILL PLAY AUDIO OF LENGTH "+(position.second-position.first)+" AT OFFSET "+position.first);
                ((Activity)context).runOnUiThread(() -> {
                    MainActivity.setDisplayStream(creation.getLength(),creation.projectPaths.modulation,false,0,Short.MAX_VALUE*2+1);
                    display.setVisibility(View.GONE);
                    seek.setVisibility(View.VISIBLE);
                });
        }).start());
    }
}

