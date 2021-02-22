package com.example.voicemodulation.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import com.example.voicemodulation.R;
import com.example.voicemodulation.audio.AudioF;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.graph.AudioDisplay;
import com.example.voicemodulation.graph.GraphLogic;

//TODO know why are you doing this? -> so i don't have to deal with fragment life cycles
//  don't have to deal with back stack
//  can get rid of implementation 'androidx.fragment:fragment:1.2.5'
//  get more shit directly in activity scope so things are easier like adding removing views and getting data from them
//      via getter/setters instead of Bundle or LiveData or more Google dev vomit

//TODO another consequence is that we can now make a functional interface
// and inner classes that implement that interface for all modulate
// functions and pass by direct reference instead of invoking the method

//TODO make this a horizontal view

//TODO now you can automatically set the gravity if ?
public class RControls extends LinearLayout {
    private HorizontalScrollView mod;
    private AudioDisplay display;
    private GraphLogic graph;
    private ImageButton record, play, pause, stop;
    private SeekBar seek;
    private Controller playback;
    private Controller sample;
    private Controller format;
    private Controller channel;
    private Controller encoding;
    private AudioF creation;
    private String name;
    private int gravity;
    private int[] scale;
    private String[] quantity_type;
    private String[] titles;
    private int[] maxes;
    private int[] progresses;


    public RControls(Context context) {
        super(context);
        init(context,null);
    }
    public RControls(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }
    public RControls(Context context, String[] _titles, int[] _maxes,
                     int[] _scale, String[] _quantity_type, int _gravity,
                     String _name, int[] _progresses, FrameLayout controls,
                     GraphLogic graph, FrameLayout seek_n_load, HorizontalScrollView modulations){
        super(context);
        this.name= _name;
        this.gravity=_gravity;
        this.scale= _scale;
        this.quantity_type=_quantity_type;
        this.titles=_titles;
        this.maxes=_maxes;
        this.mod = modulations;
        this.progresses=_progresses;
        this.record = controls.findViewById(R.id.start_recording);
        this.play = controls.findViewById(R.id.play_recording);
        this.pause = controls.findViewById(R.id.pause_recording);
        this.stop = controls.findViewById(R.id.stop_recording);
        this.graph = graph;
        this.display = seek_n_load.findViewById(R.id.audio_display);
        this.seek = seek_n_load.findViewById(R.id.seek);
        init(context,null);
    }
    public void init(Context context,@Nullable AttributeSet attrs)
    {
        // TODO i need to set title for set of controls
        //TextView type = new TextView(context);
        //type.setText("Record Controls");
        //addView(type);

        creation = new AudioF();
        playback = new Controller(getContext(),null,quantity_type[0],scale[0]);
        playback.setParam(titles[0],maxes[0],progresses[0]);
        addView(playback);

        sample = new Controller(getContext(),null,quantity_type[1],scale[1]);
        sample.setParam(titles[1],maxes[1],progresses[1]);
        addView(sample);

        ControlCases.formatSeeker fo = new ControlCases.formatSeeker();
        format = new Controller(getContext(),null,quantity_type[2],scale[2]);
        ControlCases.seekers format_seek = (a) -> String.valueOf(fo.quanToType(a));
        format.setTypeSwitch(format_seek);
        format.setZeroCase(false);
        format.setParam(titles[2],maxes[2],progresses[2]);
        addView(format);

        ControlCases.channelSeeker ch = new ControlCases.channelSeeker();
        channel = new Controller(getContext(),null,quantity_type[3],scale[3]);
        ControlCases.seekers channel_seek = (a) -> String.valueOf(ch.quanToType(a));
        channel.setTypeSwitch(channel_seek);
        channel.setZeroCase(false);
        channel.setParam(titles[3],maxes[3],progresses[3]);
        addView(channel);

        ControlCases.encodingSeeker en = new ControlCases.encodingSeeker();
        encoding = new Controller(getContext(),null,quantity_type[4],scale[4]);
        ControlCases.seekers encoding_seek = (a) -> String.valueOf(en.quanToType(a));
        encoding.setTypeSwitch(encoding_seek);
        encoding.setZeroCase(false);
        encoding.setParam(titles[4],maxes[4],progresses[4]);
        addView(encoding);

    }
    public AudioF getCreationData(){
        creation.setPlaybackRate(playback.getProgress()*scale[0]);
        creation.setSampleRate(sample.getProgress()*scale[1]);
        creation.setFormat(ControlCases.formatSeeker(format.getProgress()));
        creation.setNumChannelsIn(ControlCases.channelSeeker(channel.getProgress()));
        creation.setBitDepth(ControlCases.encodingSeeker(encoding.getProgress()));
        creation.setFilePath(creation.getNewRecordFile());
        return creation;
    }

}
