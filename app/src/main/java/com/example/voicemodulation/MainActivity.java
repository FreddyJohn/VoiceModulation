package com.example.voicemodulation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.media.AudioFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.Format;
import com.example.voicemodulation.graph.GraphLogic;
import com.example.voicemodulation.audio.RecordLogic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static final int seeker_multiplier = 4800;
    GraphLogic graph;
    int SELECTED_SAMPLE_RATE;
    int SELECTED_PLAYBACK_RATE;
    int SELECTED_AUDIO_ENCODING;
    int SELECTED_NUM_CHANNELS;
    int[] SELECTED_CHANNELS;
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
    private int STORAGE_PERMISSION_CODE = 1;
    private int AUDIO_PERMISSION_CODE = 2;
    private GestureDetector mGesture;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            requestNPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,"permission needed in order to save recording",STORAGE_PERMISSION_CODE);
            requestNPermission(Manifest.permission.RECORD_AUDIO,"permission needed in order to record",AUDIO_PERMISSION_CODE);
        }
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
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
        mGesture = new GestureDetector(this, mOnGesture);
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
        LinearLayout display = findViewById(R.id.display);
        display.addView(graph);
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
                intent.putExtra("AudioFile", creation);
                startActivity(intent);
            }
            return true;
        }
    };

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_recording:
                    String directory = sharedPref.getString("directory",getFilesDir().toString());
                    int file_index = sharedPref.getInt("index",1);
                    System.out.println("CHOSEN DIRECTORY: " + directory);
                    editor.putInt("index",file_index+=1);
                    //editor.apply();
                    SELECTED_FILE_NAME=directory+"/Recording "+file_index+".pcm";
                    Toast.makeText(MainActivity.this, "Now Recording",
                            Toast.LENGTH_SHORT).show();
                    SELECTED_SAMPLE_RATE = sample_rate.getProgress() * seeker_multiplier;
                    encodingSeeker(encoding.getProgress());
                    channelSeeker(num_channels.getProgress());
                    SELECTED_PLAYBACK_RATE = playback_rate.getProgress() * seeker_multiplier;
                    creation = new AudioFile(SELECTED_SAMPLE_RATE, SELECTED_PLAYBACK_RATE,
                            SELECTED_AUDIO_ENCODING, SELECTED_CHANNELS,formatSeeker(format.getProgress()));
                    formatSeeker(format.getProgress());
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
                break;
            case R.id.stop_recording:
                record.stopRecording();
                Intent intent = new Intent(MainActivity.this, ModulateActivity.class);
                intent.putExtra("AudioFile", creation);
                startActivity(intent);
                break;
            case R.id.pause_recording:
                file_state=false;
                boolean selected = sharedPref.getBoolean("selected",false);
                getUserDirectorySelection(selected);
                formatSeeker(format.getProgress());
                record.setRecordingState(true);
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
            System.out.println(";ohugiyftdrjxxchvbknl;p'[iuyoifutdjchgvbknl;p[iu98yt7rujcghvbknlopi089y7tfuych vPOT7IKUTCJGM, NKLJ;IOU");
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
                Uri uri = data.getData();
                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                        DocumentsContract.getTreeDocumentId(uri));
                String path = getPath(this, docUri);
                editor.putString("directory", path);
                editor.apply();
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
    /*
    public void setRecordName(boolean showCheckbox, final boolean needDecode) {
        LinearLayout container = new LinearLayout(getApplicationContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(containerLp);
        final EditText editText = new EditText(getApplicationContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(lp);
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) {
                if (s.length() > 50) {
                    s.delete(s.length() - 1, s.length());
                }
            }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_medium));

        int pad = (int) getResources().getDimension(R.dimen.spacing_normal);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(editText.getLayoutParams());
        params.setMargins(pad, pad, pad, pad);
        editText.setLayoutParams(params);
        container.addView(editText);
        if (showCheckbox) {
            container.addView(createCheckerView());
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("New name")
                .setView(container)
                .setPositiveButton("save", (dialog, id) -> {
                    String newName = editText.getText().toString();
                    editText.setText(newName);
                    if (!creation.getFilePath().equalsIgnoreCase(newName)) {
                        SELECTED_FILE_NAME=getFilesDir().getPath()+newName+".pcm";
                        System.out.println(newName);
                    }
                    //dialog.dismiss();
                })
                .setNegativeButton("cancel", (dialog, id) -> {
                    //no_name=true;
                    //dialog.dismiss();

                })
                .create();
        alertDialog.show();
        alertDialog.setOnDismissListener(dialog -> hideKeyboard());
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
        showKeyboard();
    }
    public CheckBox createCheckerView() {
        final CheckBox checkBox = new CheckBox(getApplicationContext());
        int color = getResources().getColor(R.color.black);
        checkBox.setTextColor(color);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, // unchecked
                        new int[]{android.R.attr.state_checked}  // checked
                },
                new int[]{color, color}
        );
        checkBox.setButtonTintList(colorStateList);
        checkBox.setText("Don't ask again");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int PADD = (int) getResources().getDimension(R.dimen.spacing_normal);
        params.setMargins(PADD, 0, PADD, PADD);
        checkBox.setLayoutParams(params);
        checkBox.setSaveEnabled(false);
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        checkBox.setPadding(
                checkBox.getPaddingLeft()+(int) getResources().getDimension(R.dimen.spacing_small),
                checkBox.getPaddingTop(),
                checkBox.getPaddingRight(),
                checkBox.getPaddingBottom());
        checkBox.setOnClickListener(v -> {
            //presenter.dontAskRename();
            no_name=true;
        });
        return checkBox;
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
*/
}