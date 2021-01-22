package com.example.voicemodulation;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.graph.GraphLogic;
import com.example.voicemodulation.audio.RecordLogic;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //private GestureDetector mGesture;
    private static final int seeker_multiplier = 4800;
    //Handler magic;
    GraphLogic graph;
    int SELECTED_SAMPLE_RATE;
    int SELECTED_PLAYBACK_RATE;
    int SELECTED_AUDIO_ENCODING;
    int SELECTED_NUM_CHANNELS;
    private PipedWriter jay;
    private PipedReader silentBob;
    private SeekBar sample_rate, encoding, format, playback_rate, num_channels,test;
    private TextView change_rate, change_encoding, change_format, change_playback_rate, change_num_channels;
    private String SELECTED_FILE_NAME = "/sdcard/Music/creation_test.pcm";
    private String display_selected_encoding, display_selected_format, display_num_channels;
    private ImageButton play_button, stop_button, record_button, pause_button;
    private RecordLogic record;
    private boolean has_recorded = false;
    private AudioFile creation;
    private Boolean file_state=true;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioCon.Pipes pipes = new AudioCon.Pipes();
        jay = pipes.getWriterObject();
        silentBob = pipes.getReaderObject();
        pipes.connectPipes(silentBob, jay);
        sample_rate = findViewById(R.id.sample_rate);
        encoding = findViewById(R.id.encoding);
        num_channels = findViewById(R.id.num_channels);
        change_rate = findViewById(R.id.selected_sample_rate);
        change_encoding = findViewById(R.id.selected_encoding);
        change_format = findViewById(R.id.selected_format);
        change_playback_rate = findViewById(R.id.selected_playback_rate);
        change_num_channels = findViewById(R.id.selected_num_channels);
        playback_rate = findViewById(R.id.playback_rate);
        format = findViewById(R.id.format);

        //mGesture = new GestureDetector(this, mOnGesture);
        //record = new RecordLogic(SELECTED_FILE_NAME);
        record = new RecordLogic();
        record.setPipedWriter(jay);
        graph = new GraphLogic(this);
        play_button = findViewById(R.id.play_recording);
        stop_button = findViewById(R.id.stop_recording);
        record_button = findViewById(R.id.start_recording);
        pause_button = findViewById(R.id.pause_recording);
        play_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        pause_button.setVisibility(View.INVISIBLE);
        //ParamLogic paramLogic = new ParamLogic(); //(this, null);
        //com.example.voicemodulation.widgets.test t = new test(this,null);
        //t.setAttr(this);
        //paramLogic.getParams()
        LinearLayout display = findViewById(R.id.display);
        LinearLayout controls = findViewById(R.id.record_params);
        //int balls = 0;

        //paramLogic.setParams(this,"balls",1,42,1,controls,balls,0);
        //System.out.println(balls);
        display.addView(graph);
        //controls.addView(t);
        //test = new SeekBar(this);
        //display.addView(test);

        //btnDelete.setEnabled(true);
        //btnRecord.setImageResource(R.drawable.ic_pause_circle_filled);

        //record.setFilePath(SELECTED_FILE_NAME);

        playback_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                change_playback_rate.setText(playback_rate.getProgress() * seeker_multiplier + " F/s");
                SELECTED_PLAYBACK_RATE = playback_rate.getProgress() * seeker_multiplier;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        format.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                change_format.setText(formatSeeker(format.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        encoding.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                change_encoding.setText(encodingSeeker(encoding.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        num_channels.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                change_num_channels.setText(channelSeeker(num_channels.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        sample_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                change_rate.setText(sample_rate.getProgress() * seeker_multiplier + " F/s");
                SELECTED_SAMPLE_RATE = sample_rate.getProgress() * seeker_multiplier;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled= super.dispatchTouchEvent(ev);
        handled = mGesture.onTouchEvent(ev);
        return handled;
    }
    private final GestureDetector.OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
    {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (has_recorded)
            {
                Intent intent = new Intent(MainActivity.this, ModulateActivity.class);
                intent.putExtra("SELECTED_PLAYBACK_RATE", SELECTED_PLAYBACK_RATE);
                intent.putExtra("SELECTED_AUDIO_ENCODING", SELECTED_AUDIO_ENCODING);
                intent.putExtra("FILE_NAME",SELECTED_FILE_NAME);
                startActivity(intent);
            }
            return true;
        }
    };

     */
    public String encodingSeeker(int progress) {
        switch (progress) {
            case 2:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_FLOAT;
                display_selected_encoding = "PCM Float";
                break;
            case 1:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
                display_selected_encoding = "16 Bit";
                break;
            case 0:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
                display_selected_encoding = "8 Bit";
                break;
            default:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_DEFAULT;
                display_selected_encoding = "16 Bit";
        }
        return display_selected_encoding;
    }

    public String formatSeeker(int progress) {
        switch (progress) {
            case 2:
                display_selected_format = ".wav";
                break;
            case 1:
                display_selected_format = ".mp4";
                break;
            case 0:
                display_selected_format = ".pcm";
                break;
        }
        return display_selected_format;
    }

    public String channelSeeker(int progress) {
        switch (progress) {
            case 1:
                display_num_channels = "Stereo";
                SELECTED_NUM_CHANNELS = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            case 0:
                display_num_channels = "Mono";
                SELECTED_NUM_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
                break;

        }
        return display_num_channels;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_recording:

                SELECTED_SAMPLE_RATE = sample_rate.getProgress() * seeker_multiplier;
                encodingSeeker(encoding.getProgress());
                SELECTED_PLAYBACK_RATE = playback_rate.getProgress() * seeker_multiplier;
                creation = new AudioFile(SELECTED_SAMPLE_RATE, SELECTED_PLAYBACK_RATE,
                        SELECTED_AUDIO_ENCODING, SELECTED_NUM_CHANNELS, AudioFormat.CHANNEL_IN_MONO);
                creation.setFilePath(SELECTED_FILE_NAME);
                record.setFileObject(creation,file_state);
                record.setRecordingState(false);
                record.startRecording();
                graph.setGraphState(true, silentBob);
                play_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.VISIBLE);
                record_button.setVisibility(View.INVISIBLE);
                pause_button.setVisibility(View.VISIBLE);
                num_channels.setEnabled(false);
                format.setEnabled(false);
                encoding.setEnabled(false);
                System.out.println("YOU PRESSED RECORD");

                break;
            case R.id.stop_recording:
                record.stopRecording();
                Intent intent = new Intent(MainActivity.this, ModulateActivity.class);
                intent.putExtra("AudioFile", creation);
                startActivity(intent);

                break;
            case R.id.pause_recording:
                file_state=false;
                record.setRecordingState(true);
                System.out.println("YOU PRESSED PAUSE");
                pause_button.setVisibility(View.INVISIBLE);
                record_button.setVisibility(View.VISIBLE);


                break;
            case R.id.play_recording:
                try {
                    record.play_recording();
                    has_recorded = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }



}