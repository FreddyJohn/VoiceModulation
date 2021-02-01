package com.example.voicemodulation.controls;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.voicemodulation.ModulateLogic;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.R;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;

public class NControls extends Fragment {
    private LinkedList<Controller> controllers;
    private ImageButton play_button;
    private ImageButton stop_button;
    private ModulateLogic modulate;
    public NControls(){}
    public static NControls newInstance(String[] title,int[] maxes,double scale[], String[]
                                        quantity_type, AudioFile creation, String method,
                                        int gravity, String name) {
        NControls controls = new NControls();
        Bundle args = new Bundle();
        args.putString("name",name);
        args.putInt("gravity",gravity);
        args.putDoubleArray("scale",scale);
        args.putStringArray("quantities",quantity_type);
        args.putString("method",method);
        args.putParcelable("file",creation);
        args.putStringArray("titles",title);
        args.putIntArray("maxes",maxes);
        controls.setArguments(args);
        return controls; }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup _container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        String name = args.getString("name");
        int gravity = args.getInt("gravity");
        String method = args.getString("method");
        String[] quantities = args.getStringArray("quantities");
        double[] scale = args.getDoubleArray("scale");
        AudioFile creation = args.getParcelable("file");
        //file.setFormat(new Format.wav(file));
        int[] maxes = args.getIntArray("maxes");
        String[] titles = args.getStringArray("titles");
        final View rootView = inflater.inflate(R.layout.user_controls, _container, false);
        LinearLayout controls_view = rootView.findViewById(R.id.n_parameters);
        LayoutParams view_params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        view_params.gravity = gravity;
        controls_view.setLayoutParams(view_params);
        TextView modulation_type = rootView.findViewById(R.id.modulation_type);
        modulation_type.setText(name);
        play_button = getActivity().findViewById(R.id.play);
        stop_button = getActivity().findViewById(R.id.stop_recording);
        controllers = new LinkedList<>();
        for (int i = 0; i <titles.length ; i++) {
            Controller controller = new Controller(getContext(),null,quantities[i],scale[i]);
            controller.setParam(titles[i],maxes[i]);
            controllers.add(controller);
            controls_view.addView(controller); }
        RecordLogic recordLogic = new RecordLogic();
        creation.setFilePath(creation.getNewModulateFile());
        //creation.setFilePath("/sdcard/Music/test.pcm"); // this the modulation file that we want to play duh
        recordLogic.setFileData(creation);
        double[] params = new double[maxes.length];
        play_button.setOnClickListener(v ->  new Thread(() ->{
            //TODO each seekbar should have its own setProgress. In other words, set all Amp parameters to 1 at the onset
            for (int i = 0; i <maxes.length; i++) { params[i]=controllers.get(i).getProgress()*scale[i]; }
            modulate = new ModulateLogic(params,creation);
            try { invokeMethod(modulate.getClass().getMethod(method)); }
            catch (Exception e) { e.printStackTrace(); }
            try { recordLogic.play_recording(); }
            catch (IOException e) { e.printStackTrace(); } }).start());
        /*
        stop_button.setOnClickListener(v ->{ //new Thread(() ->{
            modulate.stackModulation();
            //creation.save();
            });//).start();});

         */
        return rootView; }
    static void invokeMethod(Method method) throws Exception { method.invoke(null); }
}

