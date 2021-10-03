package com.adams.voicemodulation.controls;

import android.content.Context;
import android.media.AudioFormat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.adams.voicemodulation.database.project.AudioData;
import com.adams.voicemodulation.graph.GraphLogic;

public class RecordControls extends LinearLayout {
    private Controller playback;
    private Controller sample;
    private Controller format;
    private Controller channel;
    private Controller encoding;
    private AudioData creation;
    private int[] scale;
    private String[] quantity_type;
    private String[] titles;
    private int[] maxes;
    private int[] progresses;


    public RecordControls(Context context) {
        super(context);
        init(context,null);
    }
    public RecordControls(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }
    public RecordControls(Context context, String[] _titles, int[] _maxes,
                          int[] _scale, String[] _quantity_type, int _gravity,
                          String _name, int[] _progresses, FrameLayout controls,
                          GraphLogic graph, FrameLayout seek_n_load, HorizontalScrollView modulations){
        super(context);
        this.scale= _scale;
        this.quantity_type=_quantity_type;
        this.titles=_titles;
        this.maxes=_maxes;
        this.progresses=_progresses;
        init(context,null);
    }
    public void init(Context context,@Nullable AttributeSet attrs)
    {
        creation = new AudioData();
        playback = new Controller(getContext(),null,quantity_type[0],scale[0]);
        playback.setParam(titles[0],maxes[0],progresses[0]);
        addView(playback);

        sample = new Controller(getContext(),null,quantity_type[1],scale[1]);
        sample.setParam(titles[1],maxes[1],progresses[1]);
        addView(sample);

        /*
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
         */


    }
    public AudioData getCreationData(){
        //creation.playback_rate=playback.getProgress()*scale[0];
        //creation.sample_rate=sample.getProgress()*scale[1];
        creation.sample_rate = 44100;
        creation.playback_rate = 44100;
        //creation.format=ControlCases.formatSeeker(format.getProgress());
        creation.format=".wav";
        //ControlCases.Channels channels = ControlCases.channelSeeker(channel.getProgress());
        //creation.num_channels_in=channels.in;
        //creation.num_channels_out=channels.out;
        creation.num_channels_in= AudioFormat.CHANNEL_IN_MONO;
        creation.num_channels_out=AudioFormat.CHANNEL_OUT_MONO;
        //creation.bit_depth=ControlCases.encodingSeeker(encoding.getProgress());
        creation.bit_depth = AudioFormat.ENCODING_PCM_16BIT;
        return creation;
    }

}
