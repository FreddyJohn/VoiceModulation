package com.example.voicemodulation.widgets;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import com.example.voicemodulation.ModulateLogic;
import com.example.voicemodulation.R;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class nControls extends Fragment implements View.OnClickListener{
    private LinearLayout controls;
    private LinkedList<Controller> controllers;
    private Method modulator;
    public nControls(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup _container, Bundle savedInstanceState) {
        controllers = new LinkedList<>();
        final View rootView =
                inflater.inflate(R.layout.user_controls, _container, false);
        controls=rootView.findViewById(R.id.n_parameters);
        return rootView;
    }
    public void setNControls(String method_name, String[] title, int[] max) throws NoSuchMethodException {
        modulator = ModulateLogic.class.getMethod(method_name,int[].class);
        for (int i = 0; i <title.length ; i++) {
            Controller controller = new Controller(getContext(),null);
            controller.setParam(title[i],max[i]);
            controllers.add(controller);
            controls.addView(controller);
        }
    }
    static void doModulation(Runnable runnable)
    {
        runnable.run();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_recording:
                int[] params = new int[controllers.size()];
                for (int i = 0; i < controllers.size(); i++) {
                    int param = controllers.get(i).getProgress();
                    params[i]=param;
                }
                break;
        }

    }
}
