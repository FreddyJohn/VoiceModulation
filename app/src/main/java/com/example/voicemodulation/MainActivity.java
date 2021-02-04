package com.example.voicemodulation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.graph.GraphLogic;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static final int seeker_multiplier = 4800;
    private GraphLogic graph;
    int SELECTED_SAMPLE_RATE;
    int SELECTED_PLAYBACK_RATE;
    int SELECTED_AUDIO_ENCODING;
    int SELECTED_NUM_CHANNELS;
    int[] SELECTED_CHANNELS;
    //private PipedWriter jay;
    //private PipedReader silentBob;
    private SeekBar sample_rate, encoding, format, playback_rate, num_channels;
    private TextView change_rate, change_encoding, change_format, change_playback_rate, change_num_channels;
    private String display_selected_encoding, display_selected_format, display_num_channels;
    private ImageButton play_button, stop_button, record_button, pause_button;
    private RecordLogic record;
    private boolean has_recorded = false;
    private AudioFile creation;
    private Boolean file_state=true;
    int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        AudioCon.Pipes pipes = new AudioCon.Pipes();
        //jay = pipes.getWriterObject();
        //silentBob = pipes.getReaderObject();
        //pipes.connectPipes(silentBob, jay);
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
        record = new RecordLogic();
        //record.setPipedWriter(jay);
        graph = findViewById(R.id.display);
        //graph = new GraphLogic(this);
        play_button = findViewById(R.id.play_recording);
        stop_button = findViewById(R.id.stop_recording);
        record_button = findViewById(R.id.start_recording);
        pause_button = findViewById(R.id.pause_recording);
        play_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        pause_button.setVisibility(View.INVISIBLE);
        //LinearLayout display = findViewById(R.id.display);
        //display.addView(graph);
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
    public String encodingSeeker(int progress) {
        switch (progress) {
            case 2:
                SELECTED_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_FLOAT;
                //AudioFormat.AAC
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
                //chosen_format = new Format.wav(creation);
                //creation.setFormat(chosen_format);
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
                //SELECTED_NUM_CHANNELS = AudioFormat.CHANNEL_OUT_STEREO;
                SELECTED_CHANNELS = new int[] {AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_OUT_STEREO};
                break;
            case 0:
                display_num_channels = "Mono";
                SELECTED_NUM_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
                SELECTED_CHANNELS = new int[] {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_OUT_MONO};
                break;

        }
        return display_num_channels;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                boolean permission = hasPermissions(this, PERMISSIONS);
                if(permission) {
                    getUserDirectorySelection(false);
                }
                return true;
            case R.id.delete:
                Toast.makeText(getApplicationContext(), "delete", Toast.LENGTH_SHORT).show();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
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
            case R.id.stop_recording:
                record.stopRecording();
                Intent intent = new Intent(MainActivity.this, ModulateActivity.class);
                intent.putExtra("AudioFile", creation);
                startActivity(intent);
                break;
            case R.id.pause_recording:
                file_state=false;
                formatSeeker(format.getProgress());
                record.setRecordingState(true);
                //TODO once you press pause recalculate the max of global seek
                //global_seek.setMax(record.getLength/creation.getSampleRate());
                System.out.println("YOU PRESSED PAUSE");
                pause_button.setVisibility(View.INVISIBLE);
                record_button.setVisibility(View.VISIBLE);
                break;
            case R.id.play_recording:
                    new Thread(()->{ try { record.play_recording();}
                    catch (IOException e) { e.printStackTrace();}}).start();
                    has_recorded = true;
                break;
        }
    }
    /*
    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
        requestNPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,"permission needed in order to save recording",STORAGE_PERMISSION_CODE);
        requestNPermission(Manifest.permission.RECORD_AUDIO,"permission needed in order to record",AUDIO_PERMISSION_CODE);
    }
        else if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){editor.putBoolean("permission", true); editor.apply();}}
    private void requestNPermission(String permission,String reason,int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage(reason)
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {permission}, code))
                    .setNegativeButton("cancel", (dialog, which) -> this.finishAffinity())
                    .create().show();
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[] {permission}, code); }
    }

     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
               // Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
    }

     */
    public void getUserDirectorySelection(boolean selected){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP & !selected){
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.putExtra("android.content.extra.SHOW_ADVANCED", true);
            i.putExtra("android.content.extra.FANCY", true);
            i.putExtra("android.content.extra.SHOW_FILESIZE", true);
            startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
            editor.putBoolean("selected", true);
            editor.apply();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 9999:
                if (data!=null) {
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path = getPath(this, docUri);
                    editor.putString("directory", path);
                    editor.apply();
                }
                break;
            default:
                break;
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}

