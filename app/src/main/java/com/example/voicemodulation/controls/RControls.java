package com.example.voicemodulation.controls;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.voicemodulation.MainActivity;
import com.example.voicemodulation.R;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.graph.AudioDisplay;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public class RControls extends Fragment {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static int[] SELECTED_CHANNELS;
    private static int SELECTED_NUM_CHANNELS;
    private static String CHOSEN_FORMAT;
    private static int SELECTED_AUDIO_ENCODING;
    private LinkedList<Controller> controllers;
    private ImageButton play_button, stop_button, record_button, pause_button;
    private SeekBar seek_bar;
    private Boolean file_state=true;
    private AudioFile creation;
    private HorizontalScrollView modulations;
    private AudioDisplay display;

    public RControls(){}
    //TODO the consequence of this is that anytime the user exits the application fragment is hidden
    //  and consequently, a redraw of GraphLogic is triggered via onSizeChanged
    @Override
    public void onPause() {
        super.onPause();
        //getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(R.id.user_controls)
        System.out.println("record fragment has entered onPause. Now hiding fragment to keep listeners alive");
    }

    public static RControls newInstance(String[] title, int[] maxes, int[] scale, String[]
            quantity_type, int gravity, String name, int[] progress) {
        RControls controls = new RControls();
        Bundle args = new Bundle();
        args.putString("name",name);
        args.putInt("gravity",gravity);
        args.putIntArray("scale",scale);
        args.putStringArray("quantities",quantity_type);
        args.putStringArray("titles",title);
        args.putIntArray("maxes",maxes);
        args.putIntArray("progress",progress);
        controls.setArguments(args);
        return controls; }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup _container, Bundle savedInstanceState) {
        super.onCreateView(inflater,_container,savedInstanceState);
        Bundle args = getArguments();
        modulations = getActivity().findViewById(R.id.modulations);
        display = getActivity().findViewById(R.id.audio_display);
        seek_bar = getActivity().findViewById(R.id.seek);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        play_button = getActivity().findViewById(R.id.play_recording);
        record_button = getActivity().findViewById(R.id.start_recording);
        pause_button = getActivity().findViewById(R.id.pause_recording);
        stop_button = getActivity().findViewById(R.id.stop_recording);
        seek_bar = getActivity().findViewById(R.id.seek);
        play_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        pause_button.setVisibility(View.INVISIBLE);
        String name = args.getString("name");
        int gravity = args.getInt("gravity");
        String[] quantities = args.getStringArray("quantities");
        int[] scale = args.getIntArray("scale");
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
        controllers = new LinkedList<>();
        RecordLogic record = new RecordLogic();
        //GraphLogic graph = getActivity().findViewById(R.id.display);
        //AudioDisplay audio_display = getActivity().findViewById(R.id.audio_display);
        for (int i = 0; i <titles.length ; i++) {
            Controller controller = new Controller(getContext(),null,quantities[i],scale[i]);
            controller.setParam(titles[i],maxes[i],progress[i]); //TODO add conditional for these cases -> encodingSeeker(params[4]),channelSeeker(params[3])formatSeeker(params[2])
            controllers.add(controller);
            controls_view.addView(controller); }
        int[] params = new int[maxes.length];
        record_button.setOnClickListener(//v -> new Thread(() -> {
                v -> {
            for (int i = 0; i < maxes.length; i++) {
                params[i] = controllers.get(i).getProgress() * scale[i];
            }
            creation = new AudioFile(params[0],params[1],
                                           encodingSeeker(params[4]),channelSeeker(params[3]),
                                           formatSeeker(params[2]));

            int file_index = sharedPref.getInt("index", 1);
            editor.putInt("index", file_index += 1);
            seek_bar.setVisibility(View.GONE);
            editor.apply();
            creation.setFilePath(creation.getNewRecordFile());
            record.setFileObject(creation, file_state);
            file_state = false;
            record.setRecordingState(false);
            record.startRecording();
            String nam = Environment.getExternalStorageDirectory().getPath()+"/data.0";
            display.setVisibility(View.VISIBLE);
            MainActivity.setGraphStream(record.buffer_size,creation.getNewRecordFile(),true);
            MainActivity.setDisplayStream(record.buffer_size,creation.getNewRecordFile(),true, 1);
            record_button.setVisibility(View.INVISIBLE);
            pause_button.setVisibility(View.VISIBLE);
            //args.putParcelable("file",creation);
        });
        stop_button.setOnClickListener(v -> {
            record.stopRecording();
        });
        pause_button.setOnClickListener(v -> {
            display.setVisibility(View.GONE);
            seek_bar.setVisibility(View.VISIBLE);
            modulations.setVisibility(View.VISIBLE);
            play_button.setVisibility(View.VISIBLE);
            stop_button.setVisibility(View.VISIBLE);
            System.out.println("YOU PRESSED PAUSE");
            file_state=false;
            record.setRecordingState(true);
            pause_button.setVisibility(View.INVISIBLE);
            record_button.setVisibility(View.VISIBLE);
            args.putParcelable("file",creation);
            args.putInt("buff_size",record.buffer_size);
            //graph.setGraphState(false,record.buffer_size);
            MainActivity.setGraphStream(record.buffer_size,creation.getNewRecordFile(),false);
            MainActivity.setDisplayStream(record.buffer_size,creation.getNewRecordFile(),false, 1);
            });


        play_button.setOnClickListener(v-> new Thread(()->{ try { record.play_recording();}
        catch (IOException e) { e.printStackTrace();}}).start());

        return rootView; }

    public int encodingSeeker(int progress) {
        switch (progress) {
            case 2:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_FLOAT;
                //AudioFormat.AAC
                break;
            case 1:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
                break;
            case 0:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
                break;
            default:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_DEFAULT;
        }
        return SELECTED_AUDIO_ENCODING;
    }

    public String formatSeeker(int progress) {
        switch (progress) {
            case 2:
                CHOSEN_FORMAT = ".wav";
                //chosen_format = new Format.wav(creation);
                //creation.setFormat(chosen_format);
                break;
            case 1:
                CHOSEN_FORMAT = ".mp4";
                break;
            case 0:
                CHOSEN_FORMAT = ".pcm";
                break;
        }
        return CHOSEN_FORMAT;
    }

    public int[] channelSeeker(int progress) {
        switch (progress) {
            case 1:
                SELECTED_CHANNELS = new int[] {AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_OUT_STEREO};
                break;
            case 0:
                SELECTED_CHANNELS = new int[] {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_OUT_MONO};
                break;

        }
        return SELECTED_CHANNELS;
    }
}
/*
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_recording:
                boolean permission = hasPermissions(this, PERMISSIONS);
                boolean selected = sharedPref.getBoolean("selected",false);
                if (!permission) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                }
                else if(permission){
                    if (!selected) { getUserDirectorySelection(selected);}
                    else if(selected) {
                            //if (!file_state){ int file_pos = global_seek.getProgress()*creation.getSampleRate();}
                            String directory = sharedPref.getString("directory", getFilesDir().toString());
                            int file_index = sharedPref.getInt("index", 1);
                            editor.putInt("index", file_index += 1);
                            editor.apply();
                            //SELECTED_FILE_NAME = directory + "/Recording " + file_index + formatSeeker(format.getProgress());;
                            Toast.makeText(MainActivity.this, "Now Recording",
                                    Toast.LENGTH_SHORT).show();
                            SELECTED_SAMPLE_RATE = sample_rate.getProgress() * seeker_multiplier;
                            encodingSeeker(encoding.getProgress());
                            channelSeeker(num_channels.getProgress());
                            SELECTED_PLAYBACK_RATE = playback_rate.getProgress() * seeker_multiplier;
                            creation = new AudioFile(SELECTED_SAMPLE_RATE, SELECTED_PLAYBACK_RATE,
                                    SELECTED_AUDIO_ENCODING, SELECTED_CHANNELS, formatSeeker(format.getProgress()));
                            formatSeeker(format.getProgress());
                            creation.setFilePath(creation.getNewRecordFile());
                            record.setFileObject(creation, file_state);
                            //TODO fix this bullshit right here
                            record.setRecordingState(false);
                            record.startRecording();
                            graph.setGraphState(true,record.buffer_size);
                            //graph.setGraphState(true,silentBob);
                            //displayFragment();
                            //graph.setGraphState(true, silentBob);
                            play_button.setVisibility(View.VISIBLE);
                            stop_button.setVisibility(View.VISIBLE);
                            record_button.setVisibility(View.INVISIBLE);
                            pause_button.setVisibility(View.VISIBLE);
                            num_channels.setEnabled(false);
                            format.setEnabled(false);
                            encoding.setEnabled(false);
                        }}
                break;
 */