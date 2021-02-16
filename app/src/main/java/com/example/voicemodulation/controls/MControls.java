package com.example.voicemodulation.controls;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.voicemodulation.MainActivity;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.ModulateLogic;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.R;
import com.example.voicemodulation.graph.AudioDisplay;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.LinkedList;

public class MControls extends Fragment {
    private LinkedList<Controller> controllers;
    private ImageButton play_button;
    private ImageButton stop_button;
    private ModulateLogic modulate;
    private AudioDisplay display;
    private SeekBar seek_bar;

    public MControls(){}
    @Override
    public void onPause() {
        super.onPause();
        System.out.println("modulate fragment has entered onPause.");
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("modulate fragment has entered onResume.");
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
    @Override
    public void onStop() {
        super.onStop();
        System.out.println("modulate fragment has entered onStop. Now removing fragment to save memory");
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("modulate fragment has entered onDestroy.");
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("modulate fragment has entered onStart.");
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }



    public static MControls newInstance(String[] title, int[] maxes, double[] scale, String[]
                                        quantity_type, AudioFile creation, String method,
                                        int gravity, String name, int[] progress) {
        MControls controls = new MControls();
        Bundle args = new Bundle();
        args.putString("name",name);
        args.putInt("gravity",gravity);
        args.putDoubleArray("scale",scale);
        args.putStringArray("quantities",quantity_type);
        args.putString("method",method);
        args.putParcelable("file",creation);
        args.putStringArray("titles",title);
        args.putIntArray("maxes",maxes);
        args.putIntArray("progress",progress);
        controls.setArguments(args);
        return controls; }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup _container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        display = getActivity().findViewById(R.id.audio_display);
        seek_bar = getActivity().findViewById(R.id.seek);
        String name = args.getString("name");
        int gravity = args.getInt("gravity");
        String method = args.getString("method");
        String[] quantities = args.getStringArray("quantities");
        double[] scale = args.getDoubleArray("scale");
        AudioFile creation = args.getParcelable("file");
        //file.setFormat(new Format.wav(file));
        int[] maxes = args.getIntArray("maxes");
        String[] titles = args.getStringArray("titles");
        int[] progress = args.getIntArray("progress");
        final View rootView = inflater.inflate(R.layout.user_controls, _container, false);
        LinearLayout controls_view = rootView.findViewById(R.id.n_parameters);
        FrameLayout.LayoutParams view_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        view_params.gravity = gravity;
        controls_view.setLayoutParams(view_params);
        TextView modulation_type = rootView.findViewById(R.id.modulation_type);
        modulation_type.setText(name);
        play_button = getActivity().findViewById(R.id.play_recording);
        stop_button = getActivity().findViewById(R.id.stop_recording);
        AudioCon.IO_RAF con = new AudioCon.IO_RAF(creation.getNewModulateFile());
        RandomAccessFile f = con.getReadObject();
        int length=0;
        try {
             length= (int) f.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controllers = new LinkedList<>();
        for (int i = 0; i <titles.length ; i++) {
            Controller controller = new Controller(getContext(),null,quantities[i],scale[i]);
            controller.setParam(titles[i],maxes[i],progress[i]);
            controllers.add(controller);
            controls_view.addView(controller); }
        RecordLogic recordLogic = new RecordLogic();
        creation.setFilePath(creation.getNewModulateFile());
        //creation.setFilePath("/sdcard/Music/test.pcm"); // this the modulation file that we want to play duh
        recordLogic.setFileData(creation);
        double[] params = new double[maxes.length];
        int finalLength = length;
        //boolean activated = display.isActivated();
        System.out.println("length of file in bytes: "+length);
        seek_bar.setVisibility(View.GONE);
        getActivity().runOnUiThread(() -> {
            try {
                MainActivity.setDisplayStream(finalLength, creation.getNewModulateFile(), false, 0);
            }
            catch (NullPointerException e){}});
        play_button.setOnClickListener(v -> new Thread(() ->{
            for (int i = 0; i <maxes.length; i++) { params[i]=controllers.get(i).getProgress()*scale[i]; }
            modulate = new ModulateLogic(params,creation);
            try{
                /*
                getActivity().runOnUiThread(() -> {
                    if (activated){
                        MainActivity.setDisplayStream(finalLength,creation.getNewModulateFile(),false,0);
                        display.setActivated(false);}});

                 */
                getActivity().runOnUiThread(() ->{
                    System.out.println("display setting visible");
                    //seek_bar.setVisibility(View.GONE);
                    //display.setActivated(false);
                    //if (!activated){
                     //   display.setActivated(true);
                    display.setVisibility(View.VISIBLE);
                    MainActivity.setDisplayStream(finalLength,creation.getNewModulateFile(),true, 0);
               // }
                //if (a==true){display.setActivated(false);}
                //display.setVisibility(View.VISIBLE);
               // MainActivity.setDisplayStream(finalLength,creation.getNewModulateFile(),true, 0);
                });}

            catch (NullPointerException e){}
            try { invokeMethod(modulate.getClass().getMethod(method)); }
            catch (Exception e) { e.printStackTrace(); }
            try {
                recordLogic.play_recording();
                getActivity().runOnUiThread(() ->{
                    //System.out.println("display setting gone");
                    //display.setVisibility(View.GONE);
                    //seek_bar.setVisibility(View.VISIBLE);
                    //display.setActivated(false);
                    //if (display.isActivated()){
                    //MainActivity.setDisplayStream(finalLength,creation.getNewModulateFile(),false,0);
                    //    display.setActivated(false);}
                });
            } catch (IOException e) { e.printStackTrace(); }
              catch (NullPointerException e){ e.printStackTrace(); }}).start());
        play_button.setOnLongClickListener(v -> {
            try {
                //getActivity().runOnUiThread(() -> MainActivity.setDisplayStream(1000,creation.getNewRecordFile(),true,0));
                creation.setFilePath(creation.getNewRecordFile());
                recordLogic.setFileData(creation);
                recordLogic.play_recording();
                creation.setFilePath(creation.getNewModulateFile());
                recordLogic.setFileData(creation);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        });
        stop_button.setOnLongClickListener(v ->{ new Thread(creation::save).start();
           System.out.println("I SAVED");
           return true;
        });
        return rootView; }
    static void invokeMethod(Method method) throws Exception { method.invoke(null); }
}

