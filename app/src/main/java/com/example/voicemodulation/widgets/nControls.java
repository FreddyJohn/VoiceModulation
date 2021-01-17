package com.example.voicemodulation.widgets;
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
import java.util.LinkedList;

public class nControls extends Fragment {
    private LinkedList<Controller> controllers;
    private ModulateLogic.Parameters myParamFunc;
    private ImageButton play_button;
    public nControls(){

    }
    //https://developer.android.com/guide/fragments/communicate
    public static nControls newInstance(String[] title, int[] maxes,ModulateLogic.Parameters parameters, AudioFile creation) {
        nControls controls = new nControls();
        Bundle args = new Bundle();
        args.putParcelable("params", (Parcelable) parameters);
        args.putParcelable("type",creation);
        args.putStringArray("titles",title);
        args.putIntArray("maxes",maxes);
        controls.setArguments(args);
        return controls;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup _container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ModulateLogic.Parameters parameters = args.getParcelable("type");
        AudioFile file = args.getParcelable("type");
        int[] maxes = args.getIntArray("maxes");
        String[] titles = args.getStringArray("titles");
        final View rootView = inflater.inflate(R.layout.user_controls, _container, false);
        LinearLayout controls = rootView.findViewById(R.id.n_parameters);
        play_button = rootView.findViewById(R.id.play);
        controllers = new LinkedList<>();
        for (int i = 0; i <titles.length ; i++) {
            Controller controller = new Controller(getContext(),null);
            controller.setParam(titles[i],maxes[i]);
            controllers.add(controller);
            controls.addView(controller);
        }
        RecordLogic recordLogic = new RecordLogic();
        ModulateLogic modulate = new ModulateLogic(file.getPlaybackRate(), file.getBitDepth(), file.getFilePath());
        file.setFilePath("/sdcard/Music/test.pcm");
        recordLogic.setFileObject(file);
        play_button.setOnClickListener(v -> {
            int[] params = new int[maxes.length];
            for (int i = 0; i <maxes.length; i++) {
               params[i]=controllers.get(i).getProgress();
            }
            parameters.setParameters(params);
            //modulate.makeEchoCreation(params);
            try {
                recordLogic.play_recording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return rootView;

    }

}
