package com.example.voicemodulation;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    int SELECTED_SAMPLE_RATE;
    int SELECTED_PLAYBACK_RATE;
    int SELECTED_AUDIO_ENCODING;
    private GestureDetector mGesture;
    private static final int seeker_multiplier = 4800;
    private SeekBar sample_rate,encoding,format,playback_rate;
    private TextView change_rate,change_encoding,change_format,change_playback_rate;
    private String SELECTED_FILE_NAME ="/sdcard/Music/creation_test.pcm";
    private RecordLogic record;
    private boolean has_recorded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sample_rate = findViewById(R.id.sample_rate);
        encoding = findViewById(R.id.encoding);
        change_rate = findViewById(R.id.selected_sample_rate);
        change_encoding = findViewById(R.id.selected_encoding);
        change_format = findViewById(R.id.selected_format);
        change_playback_rate = findViewById(R.id.selected_playback_rate);
        playback_rate = findViewById(R.id.playback_rate);
        format = findViewById(R.id.format);
        mGesture = new GestureDetector(this, mOnGesture);
        record = new RecordLogic(this);
        record.setFilePath(SELECTED_FILE_NAME);
        playback_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                change_playback_rate.setText(String.valueOf(playback_rate.getProgress()* seeker_multiplier));
                SELECTED_PLAYBACK_RATE=playback_rate.getProgress()* seeker_multiplier;

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        format.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                change_format.setText(String.valueOf(format.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        encoding.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                change_encoding.setText(String.valueOf(encoding.getProgress()));
                encodingSeeker(encoding.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        sample_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                change_rate.setText(String.valueOf(sample_rate.getProgress()* seeker_multiplier));
                SELECTED_SAMPLE_RATE=sample_rate.getProgress()* seeker_multiplier;

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled= super.dispatchTouchEvent(ev);
        handled = mGesture.onTouchEvent(ev);
        return handled;
    }
    private GestureDetector.OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
    {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (has_recorded!=false)
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
    public void encodingSeeker(int progress)
    {
        switch (progress)
        {
            case 2:
                SELECTED_AUDIO_ENCODING=AudioFormat.ENCODING_PCM_FLOAT;
                break;
            case 1:
                SELECTED_AUDIO_ENCODING= AudioFormat.ENCODING_PCM_16BIT;
                break;
            case 0:
                SELECTED_AUDIO_ENCODING=AudioFormat.ENCODING_PCM_8BIT;
                break;
            default:
                SELECTED_AUDIO_ENCODING=AudioFormat.ENCODING_DEFAULT;
        }
    }
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.start_recording:
                SELECTED_SAMPLE_RATE=sample_rate.getProgress() * seeker_multiplier;
                encodingSeeker(encoding.getProgress());
                record.setAudioEncoding(SELECTED_AUDIO_ENCODING);
                record.setSampleRate(SELECTED_SAMPLE_RATE);
                record.start_recording();
                break;
            case R.id.stop_recording:
                record.stop_recording();

                break;
            case R.id.play_recording:
                record.setFilePath(SELECTED_FILE_NAME);
                SELECTED_PLAYBACK_RATE=playback_rate.getProgress() * seeker_multiplier;
                record.setPlayBackRate(SELECTED_PLAYBACK_RATE);
                try {
                    record.play_recording();
                    has_recorded=true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}