package com.example.voicemodulation.controls;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.voicemodulation.audio.AudioF;
import com.example.voicemodulation.audio.ModulateLogic;
import com.example.voicemodulation.audio.RecordLogic;

import java.io.IOException;
import java.util.LinkedList;

public class MControls extends LinearLayout{
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
                     int gravity, String name, int[] progress, ImageButton play){
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
        play.setOnClickListener(v->{ new Thread(() -> {
            RecordLogic recordLogic = new RecordLogic();
            creation.setFilePath(creation.getNewModulateFile());
            recordLogic.setFileData(creation);
            method.modulate(getModulateParameters(),creation);
            try {
                recordLogic.play_recording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();});
    }
}
/*
v -> new Thread(() ->{
            for (int i = 0; i <maxes.length; i++) { params[i]=controllers.get(i).getProgress()*scale[i]; }
            modulate = new ModulateLogic(params,creation);
            try{
                getActivity().runOnUiThread(() ->{
                    seek_bar.setVisibility(View.GONE);
                    display.setVisibility(View.VISIBLE);
                    test.setDisplayStream(finalLength,creation.getNewModulateFile(),true, 0,Short.MAX_VALUE*2+1);
                });}
            catch (NullPointerException e){}
            try {
                invokeMethod(modulate.getClass().getMethod(method)); }
            catch (Exception e) {}
            try {
                recordLogic.play_recording();
                getActivity().runOnUiThread(() ->{
                    display.setVisibility(View.GONE);
                    seek_bar.setVisibility(View.VISIBLE);
                    test.setDisplayStream(finalLength,creation.getNewModulateFile(),false,0,Short.MAX_VALUE*2+1);
                });
            } catch (IOException e) { e.printStackTrace(); }
              catch (NullPointerException e){ e.printStackTrace(); }}).start());
 */


/*
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
        MainActivity test = new MainActivity();
        //creation.setFilePath("/sdcard/Music/test.pcm"); // this the modulation file that we want to play duh
        //recordLogic.setFileData(creation);
        double[] params = new double[maxes.length];
        int finalLength = length;
        boolean activated = display.isActivated();
        System.out.println("length of file in bytes: "+length);
        //seek_bar.setVisibility(View.GONE);
        getActivity().runOnUiThread(() -> {
            try {
                test.setDisplayStream(finalLength, creation.getNewModulateFile(), false, 0,Short.MAX_VALUE*2+1);
            }
            catch (NullPointerException e){}});
        play_button.setOnClickListener(v -> new Thread(() ->{
            for (int i = 0; i <maxes.length; i++) { params[i]=controllers.get(i).getProgress()*scale[i]; }
            modulate = new ModulateLogic(params,creation);
            try{
                getActivity().runOnUiThread(() ->{
                    seek_bar.setVisibility(View.GONE);
                    display.setVisibility(View.VISIBLE);
                    test.setDisplayStream(finalLength,creation.getNewModulateFile(),true, 0,Short.MAX_VALUE*2+1);
                });}
            catch (NullPointerException e){}
            try {
                invokeMethod(modulate.getClass().getMethod(method)); }
            catch (Exception e) {}
            try {
                recordLogic.play_recording();
                getActivity().runOnUiThread(() ->{
                    display.setVisibility(View.GONE);
                    seek_bar.setVisibility(View.VISIBLE);
                    test.setDisplayStream(finalLength,creation.getNewModulateFile(),false,0,Short.MAX_VALUE*2+1);
                });
            } catch (IOException e) { e.printStackTrace(); }
              catch (NullPointerException e){ e.printStackTrace(); }}).start());
        play_button.setOnLongClickListener(v -> {
            try {
                //getActivity().runOnUiThread(() -> MainActivity.setDisplayStream(1000,creation.getNewRecordFile(),true,0));
                creation.setFilePath(creation.getNewRecordFile());
               // recordLogic.setFileData(creation);
                recordLogic.play_recording();
                creation.setFilePath(creation.getNewModulateFile());
             //   recordLogic.setFileData(creation);
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

 */
/*

                getActivity().runOnUiThread(() -> {
                    if (activated){
                        MainActivity.setDisplayStream(finalLength,creation.getNewModulateFile(),false,0);
                        display.setActivated(false);}});
                    //display.setActivated(false);
                    //if (!activated){
                     //   display.setActivated(true);
               // }
                //if (a==true){display.setActivated(false);}
                //display.setVisibility(View.VISIBLE);
               // MainActivity.setDisplayStream(finalLength,creation.getNewModulateFile(),true, 0);


 */

