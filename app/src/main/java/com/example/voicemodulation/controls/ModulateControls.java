package com.example.voicemodulation.controls;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.example.voicemodulation.MainActivity;
import com.example.voicemodulation.R;
import com.example.voicemodulation.database.project.Project;
import com.example.voicemodulation.signal.Modulation;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.graph.AudioDisplay;
import com.example.voicemodulation.structures.Structure;
import com.example.voicemodulation.util.FileUtil;

import java.util.LinkedList;

public class ModulateControls extends LinearLayout{
    private FrameLayout info;
    private Structure pieceTable;
    private AudioDisplay display;
    private ImageButton play;
    private LinkedList<Controller> controllers;
    private String[] title;
    private int[] maxes;
    private double[] scale;
    private  String[] quantity_type;
    private Project project;
    private Modulation.modulation method;
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
                            String[] quantity_type, Modulation.modulation modulation,
                            String name, int[] progress, ImageButton play,
                            FrameLayout info){
        super(context);
        this.title=title;
        this.maxes=maxes;
        this.scale=scale;
        this.quantity_type=quantity_type;
        this.project = MainActivity.newProject;
        this.method=modulation;
        this.name=name;
        this.progress=progress;
        this.play = play;
        this.info = info;
        this.display = info.findViewById(R.id.audio_display);
        //this.pieceTable = pieceTable;
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
        TextView control_title = ((Activity)context).findViewById(R.id.control_title);
        control_title.setText(name);
        for (int i = 0; i <title.length ; i++) {
            Controller controller = new Controller(getContext(),null,quantity_type[i],scale[i]);
            controller.setParam(title[i],maxes[i],progress[i]);
            controllers.add(controller);
            addView(controller); }
        play.setOnLongClickListener(v -> {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    position = MainActivity.getSelectionPoints();
                    method.modulate(getModulateParameters(), MainActivity.newProject, position, MainActivity.audioPieceTable,null);
                    FileUtil.writeModulation(MainActivity.newProject,MainActivity.audioPieceTable,position);
                }
            };
            thread.start();
            Toast.makeText(getContext(), "Effect written to Disk", Toast.LENGTH_SHORT).show();
            return true;
        });

        play.setOnClickListener(v -> {
            Thread thread = new Thread() {
                private RecordLogic recordLogic;
                @Override
                public void run() {
                    recordLogic = new RecordLogic();
                    position = MainActivity.getSelectionPoints();
                    int length = MainActivity.getLength();
                    //position = new Pair<>(bytePoints.audio_start,bytePoints.audio_stop);
                    recordLogic.setPieceTable(null);
                    //recordLogic.setFileData(creation, creation.projectPaths.modulation);
                    recordLogic.setFileData(MainActivity.newProject.audioData, MainActivity.newProject.paths.modulation);
                    method.modulate(getModulateParameters(), MainActivity.newProject, position, MainActivity.audioPieceTable,null);
                    while (!Thread.currentThread().isInterrupted()) {
                        ((Activity) context).runOnUiThread(() -> {
                            display.setVisibility(View.VISIBLE);
                            info.findViewById(R.id.memory).setVisibility(View.GONE);
                            info.findViewById(R.id.time).setVisibility(View.GONE);
                            info.findViewById(R.id.freq).setVisibility(View.GONE);
                            MainActivity.setDisplayStream(length, MainActivity.newProject.paths.modulation, true, Short.MAX_VALUE * 2 + 1,0);
                        });
                        recordLogic.play_recording(0, position.second - position.first);
                        ((Activity) context).runOnUiThread(() -> {
                            MainActivity.setDisplayStream(length, MainActivity.newProject.paths.modulation, false, Short.MAX_VALUE * 2 + 1,0);
                            display.setVisibility(View.GONE);
                            info.findViewById(R.id.memory).setVisibility(View.VISIBLE);
                            info.findViewById(R.id.time).setVisibility(View.VISIBLE);
                            info.findViewById(R.id.freq).setVisibility(View.VISIBLE);

                        });
                        break;
                    }
                }
                @Override
                public void interrupt() {
                    super.interrupt();
                    recordLogic.stop_playing();
                }
            };
            ((Activity) context).runOnUiThread(() -> 
                    MainActivity.addThread(thread)); 
            thread.start();
        });

        /*
        play.setOnClickListener(v -> new Thread(() -> {
            ((Activity) context).runOnUiThread(() -> {
                seek.setVisibility(View.GONE);
                display.setVisibility(View.VISIBLE);
                MainActivity.setDisplayStream(creation.getLength(), creation.projectPaths.modulation, true, 0, Short.MAX_VALUE * 2 + 1);
            });
            position = MainActivity.getSelectionPoints();
            RecordLogic recordLogic = new RecordLogic();
            recordLogic.setPieceTable(null);
            //creation.setFilePath(creation.getNewModulateFile());
            //recordLogic.setFileData(creation);
            recordLogic.setFileData(creation, creation.projectPaths.modulation);
            method.modulate(getModulateParameters(), creation, position, pieceTable);
            //System.out.println("NO NULL POINTER HAVE SCOPE "+seek.getProgress());
            //recordLogic.play_recording(0,creation.getLength());
            recordLogic.play_recording(0, position.second - position.first, context);
            System.out.println("WE WILL PLAY AUDIO OF LENGTH " + (position.second - position.first) + " AT OFFSET " + position.first);
            ((Activity) context).runOnUiThread(() -> {
                MainActivity.setDisplayStream(creation.getLength(), creation.projectPaths.modulation, false, 0, Short.MAX_VALUE * 2 + 1);
                display.setVisibility(View.GONE);
                seek.setVisibility(View.VISIBLE);
            });
        }).start());

         */
    }
    
}

