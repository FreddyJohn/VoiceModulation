package com.example.voicemodulation;
import androidx.appcompat.app.AppCompatActivity;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.View;
import java.io.IOException;

public class ModulateActivity extends AppCompatActivity implements View.OnClickListener {
    private static int PLAYBACK_SAMPLE_RATE;
    private static int SELECTED_AUDIO_ENCODING;
    private ModulateLogic modulate;
    private String SELECTED_FILE_NAME;
    private RecordLogic player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);
        PLAYBACK_SAMPLE_RATE=getIntent().getIntExtra("SELECTED_PLAYBACK_RATE",44100);
        SELECTED_AUDIO_ENCODING=getIntent().getIntExtra("SELECTED_AUDIO_ENCODING", AudioFormat.ENCODING_PCM_16BIT);
        SELECTED_FILE_NAME=getIntent().getStringExtra("FILE_NAME");
        modulate = new ModulateLogic(this,PLAYBACK_SAMPLE_RATE,SELECTED_AUDIO_ENCODING,SELECTED_FILE_NAME);
        player = new RecordLogic(this);
        player.setFilePath("/sdcard/Music/test.pcm");
        player.setPlayBackRate(PLAYBACK_SAMPLE_RATE);
        player.setAudioEncoding(SELECTED_AUDIO_ENCODING);
    }
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.backwards:
                try {
                    modulate.makeBackwardsCreation();
                    player.play_recording();
                    //modulate.doSomething("/sdcard/Music/gucci.pcm");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button:
                try {
                    modulate.makeEchoCreation();
                    player.play_recording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
