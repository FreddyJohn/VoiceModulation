package com.example.voicemodulation;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.controls.MControls;
import com.example.voicemodulation.controls.RControls;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private final String[] record_control_titles = new String[] {"PlayBack Rate","Sample Rate","Format","Channels","Encoding"};
    private final String[] phaser_titles = new String[] {"Frequency","Carrier Amp","Modulator Amp","Theta"};
    private final String[] record_control_quantities = new String[]{"Hz","Hz",".wav","mono","Bit"};
    private final String[] phaser_quantities = new String[] {"Hz","Amp","Amp","θ"};
    private final String[] flanger_titles = new String[] {"Min","Max","Frequency"};
    private final String[] echo_titles = new String[] {"Signals","Delay"};
    private final int[] record_control_ranges = new int[]{10,10,3,2,2};
    private final int[] record_control_scales = new int[]{4800,4800,1,1,1};
    private final int[] record_control_progresses = new int[]{10,10,1,1,1};
    private final int[] phaser_progress = new int[] {1,10,10,0};
    private final int[] flanger_progress = new int[] {8,4,1};
    private final int record_gravity = Gravity.NO_GRAVITY;
    private final String record_control_title = "Record Controls";
    private HorizontalScrollView modulations;
    private RControls record_controls;
    private boolean initial_record_transaction;
    int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions( PERMISSIONS, PERMISSION_ALL);
        }
        record_controls = displayRFragment(record_control_titles,record_control_ranges,
                                                    record_control_scales,record_control_quantities,
                                                    record_gravity,record_control_title,record_control_progresses);
        modulations = findViewById(R.id.modulations);
        modulations.setVisibility(View.INVISIBLE);
        //Bundle record_parameters = record_controls.getArguments();


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


    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public RControls displayRFragment(String[] titles, int[] maxes, int[] scale, String[] quantity_type, int gravity, String name, int[] progress) {
        RControls controls = RControls.newInstance(titles,maxes,scale,quantity_type,gravity,name,progress);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.user_controls,
                controls).commit();
        initial_record_transaction=true;
        return controls; }
    public void closeFragment(int resource_id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment controller_type = fragmentManager
                .findFragmentById(resource_id);
        if (controller_type != null) {
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            //TODO hide vs remove I do not want a bunch of modulation fragment initializations just hanging out
            fragmentTransaction.hide(controller_type).commit(); } }

    public MControls displayMFragment(String[] titles, int[] maxes, double[] scale, String[] quantity_type,AudioFile creation,
                                     String method, int gravity, String name, int[] progress) {
        MControls controls = MControls.newInstance(titles,maxes,scale,quantity_type,creation,method,gravity,name,progress);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.user_controls,
                controls).commit();
        return controls; }
    @Override
    public void onClick(View v) {
        Bundle record_parameters = record_controls.getArguments();
        AudioFile creation = record_parameters.getParcelable("file");
        double nyquist = (creation.getSampleRate() / 2) / 20;
        closeFragment(R.id.user_controls);
        switch (v.getId()) {
            case R.id.backwards:
                String[] backwards_titles = new String[]{"Volume"};
                int[] backwards_maxes = new int[]{10};
                displayMFragment(backwards_titles, backwards_maxes, new double[]{.1},
                        new String[]{"Volume"}, creation, "makeBackwardsCreation", Gravity.CENTER,
                        "Backwards Effect", new int[]{10});
                break;
            case R.id.echo:
                int[] echo_maxes = new int[]{10, 10};
                displayMFragment(echo_titles, echo_maxes, new double[]{1, 1},
                        new String[]{"S", "D"}, creation, "makeEchoCreation", Gravity.CENTER,
                        "Echo Effect", new int[]{5, 6});
                break;
            case R.id.quantize:
                String[] robotic_titles = new String[]{"Quantize", "Amplitude"};
                int[] robotic_maxes = new int[]{10, 10};
                displayMFragment(robotic_titles, robotic_maxes, new double[]{1000, .1},
                        new String[]{"C", "Amp"}, creation, "makeQuantizedCreation", Gravity.CENTER,
                        "Quantize Audio Sample", new int[]{5, 10});
                break;
            case R.id.phaser:
                int[] phaser_maxes = new int[]{20, 10, 10, 20};
                displayMFragment(phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, creation, "makePhaserCreation", Gravity.NO_GRAVITY,
                        "Phaser with Sine Wave", phaser_progress);
                break;
            case R.id.phaser_triangle:
                int[] alien_maxes = new int[]{20, 10, 10, 10};
                displayMFragment(phaser_titles, alien_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, creation, "makePhaserTriangleCreation", Gravity.NO_GRAVITY,
                        "Phaser with Triangle Wave", phaser_progress);
                break;
            case R.id.phaser_square:
                int[] square_maxes = new int[]{20, 10, 10, 10};
                displayMFragment(phaser_titles, square_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, creation, "makePhaserSquareCreation", Gravity.NO_GRAVITY,
                        "Phaser with Square Wave", phaser_progress);
                break;
            case R.id.phaser_saw:
                int[] saw_maxes = new int[]{20, 10, 10, 10};
                displayMFragment(phaser_titles, saw_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, creation, "makePhaserSawCreation", Gravity.NO_GRAVITY,
                        "Phaser with Saw Wave", phaser_progress);
                break;
            case R.id.flanger:
                int[] flanger_maxes = new int[]{10, 10, 20};
                displayMFragment(flanger_titles, flanger_maxes, new double[]{10, 10, nyquist},
                        new String[]{null, null, "Hz"}, creation, "makeFlangerCreation", Gravity.NO_GRAVITY,
                        "Flanger with Sine Wave", flanger_progress);
                break;
            case R.id.flanger_triangle:
                int[] flanger_triangle_maxes = new int[]{10, 10, 20};
                displayMFragment(flanger_titles, flanger_triangle_maxes, new double[]{10, 10, nyquist},
                        new String[]{null, null, "Hz"}, creation, "makeFlangerTriangleCreation", Gravity.NO_GRAVITY,
                        "Flanger with Triangle Wave", flanger_progress);
                break;
            case R.id.flanger_square:
                int[] flanger_square_maxes = new int[]{10, 10, 20};
                displayMFragment(flanger_titles, flanger_square_maxes, new double[]{10, 10, nyquist},
                        new String[]{null, null, "Hz"}, creation, "makeFlangerSquareCreation", Gravity.NO_GRAVITY,
                        "Flanger with Square Wave", flanger_progress);
                break;
            }

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

