package com.example.voicemodulation.controls;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import com.example.voicemodulation.ModulateLogic;
import com.example.voicemodulation.R;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;

public class NControls extends Fragment {
    private LinkedList<Controller> controllers;
    private ImageButton play_button;
    public NControls(){
    }
    public static NControls newInstance(String[] title,int[] maxes, AudioFile creation, String method,String file) {
        NControls controls = new NControls();
        Bundle args = new Bundle();
        args.putString("filepath",file);
        args.putString("method",method);
        args.putParcelable("file",creation);
        args.putStringArray("titles",title);
        args.putIntArray("maxes",maxes);
        controls.setArguments(args);
        return controls; }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup _container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        String method = args.getString("method");
        AudioFile file = args.getParcelable("file");
        String filePath = args.getString("filepath");
        int[] maxes = args.getIntArray("maxes");
        String[] titles = args.getStringArray("titles");
        final View rootView = inflater.inflate(R.layout.user_controls, _container, false);
        LinearLayout controls_view = rootView.findViewById(R.id.n_parameters);
        //LinearLayout controls_view = getActivity().findViewById(R.id.n_parameters);
        play_button = getActivity().findViewById(R.id.play);
        controllers = new LinkedList<>();
        for (int control = 0; control <titles.length ; control++) {
            Controller controller = new Controller(getContext(),null);
            controller.setParam(titles[control],maxes[control]);
            controllers.add(controller);
            controls_view.addView(controller); }
        RecordLogic recordLogic = new RecordLogic();
        System.out.println(filePath);
        file.setFilePath("/sdcard/Music/test.pcm");
        recordLogic.setFileData(file);
        play_button.setOnClickListener(v -> {
            int[] params = new int[maxes.length];
            for (int i = 0; i <maxes.length; i++) { params[i]=controllers.get(i).getProgress(); }
            ModulateLogic modulate = new ModulateLogic(params,filePath,file.getPlaybackRate());
            try { invokeMethod(modulate.getClass().getMethod(method)); }
            catch (Exception e) { e.printStackTrace(); }
            try { recordLogic.play_recording(); }
            catch (IOException e) { e.printStackTrace(); } });
        return rootView; }
    static void invokeMethod(Method method) throws Exception { method.invoke(null); }
}

