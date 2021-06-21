package com.example.voicemodulation;
import android.Manifest;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voicemodulation.project.AudioData;
import com.example.voicemodulation.modulate.TimeDomain;
import com.example.voicemodulation.project.Paths;
import com.example.voicemodulation.sequence.PieceTable;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.controls.ModulateControls;
import com.example.voicemodulation.controls.RecordControls;
import com.example.voicemodulation.graph.AudioDisplay;
import com.example.voicemodulation.graph.GraphLogic;
import com.example.voicemodulation.project.Sequences;

// TODO USER CONTROLLED VARIABILITY (support for N languages and N finger sizes)
//  (1) consider the case when the user has a finger size above or below 50dp
//  if we were to allow the user to vary the view sizes based on the upper and lower human finger diameters
//  then the calculations used by GraphLogic would all vary with this finger size selection
//  also  consider the case when the user would like to resize the waveform
//  if we change the true column height
//  then the calculations used by GraphLogic will have to be modified,
//  this is unfavorable.
//  a solution is to maintain a constant column height and use canvas.scale(x,y) in a way that is proportional to the finger selection and or resize.
//  (2) consider the case when the user would like to resize the true column height for better memory performance on their device
//  let column height = x
//  lower <= x <= upper
//  where lower and upper are dependent on the finger size selection
//  (3) language

//TODO UI IMPROVEMENTS
//  (1) find a way to remove the boxes around the vector images for each modulation and make them look more professional
//  (2) I would like modulation selection to be interactive. By this I mean the vector image should resize and shake providing some noticeable feedback
//  (3) Piggyback off of (2) make it optional and default for all user actions to be interactive to add support for disabled

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private final String[] record_control_titles = new String[] {"PlayBack Rate","Sample Rate","Format","Channels","Encoding"};
    private final String[] phaser_titles = new String[] {"Frequency","Carrier Amp","Modulator Amp","Theta"};
    private final String[] record_control_quantities = new String[]{"Hz","Hz",null,null,null};
    private final String[] phaser_quantities = new String[] {"Hz","Amp","Amp","θ"};
    private final String[] flanger_titles = new String[] {"Min","Max","Frequency"};
    private final String[] echo_titles = new String[] {"Signals","Delay"};
    private final int[] record_control_ranges = new int[]{10,10,2,1,1};
    private final int[] record_control_scales = new int[]{4800,4800,1,1,1};
    private final int[] record_control_progresses = new int[]{10,10,2,0,1};
    private final int[] phaser_progress = new int[] {1,10,10,0};
    private final int[] flanger_progress = new int[] {8,4,1};
    private final int record_gravity = Gravity.NO_GRAVITY;
    private ImageButton play_button, stop_button, record_button, pause_button;
    private TextView time;
    private static AudioDisplay display;
    private static GraphLogic graph;
    private Boolean file_state=true;
    private double nyquist =0;
    private final String record_control_title = "Record Controls";
    private HorizontalScrollView modulations;
    private FrameLayout record_controls;
    private LinearLayout seek_n_loader;
    private HorizontalScrollView scrollView;
    private HorizontalScrollView graph_scroll;

    private SeekBar the_seeker;
    private RecordControls controls;
    private RecordLogic record;
    private AudioData audioProject;
    private PieceTable pieceTable;
    int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};
    private PieceTable bitmapPieceTable;
    private Paths projectPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String bitmap_path= Environment.getExternalStorageDirectory().getPath()+"/bitmap";
        String bitmap_table_path = Environment.getExternalStorageDirectory().getPath()+"/bitmap_piece_table";
        String audio_table_path = Environment.getExternalStorageDirectory().getPath()+"/audio_piece_table";
        String original_audio_path= Environment.getExternalStorageDirectory().getPath()+"/original_audio_piece";
        String original_bitmap_path= Environment.getExternalStorageDirectory().getPath()+"/original_bitmap_piece";
        String record_path = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
        String modulation_file = Environment.getExternalStorageDirectory().getPath()+"/mod.pcm";
        projectPaths = new Paths(bitmap_path,record_path,
                                        bitmap_table_path, audio_table_path,
                                        original_audio_path,original_bitmap_path,
                                        modulation_file);
        Sequences projectSequences = new Sequences(projectPaths);
        // Data.getMemory()/x ? instead of static 1MB
        bitmapPieceTable = new PieceTable(bitmap_table_path,bitmap_path,original_bitmap_path,1000000);
        audioProject = new AudioData();
        record = new RecordLogic();
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        record_controls = findViewById(R.id.record_controls);
        record_button = findViewById(R.id.start_recording);
        play_button = findViewById(R.id.play_recording);
        pause_button = findViewById(R.id.pause_recording);
        stop_button = findViewById(R.id.stop_recording);
        play_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        pause_button.setVisibility(View.INVISIBLE);
        time = findViewById(R.id.time);
        //if (!hasPermissions(this, PERMISSIONS)) {
        //    requestPermissions( PERMISSIONS, PERMISSION_ALL);
        //}

        seek_n_loader = findViewById(R.id.seek_n_load);
        scrollView = findViewById(R.id.fuckFragments);
        display = findViewById(R.id.audio_display);
        modulations = findViewById(R.id.modulations);
        graph = findViewById(R.id.display);
        controls = new RecordControls(this,record_control_titles,record_control_ranges,
                record_control_scales,record_control_quantities,
                record_gravity,record_control_title,record_control_progresses,
                record_controls,graph,seek_n_loader,modulations);
        scrollView.addView(controls);
        the_seeker = findViewById(R.id.seek);
        //graph.createNewProject(projectPaths);
        pieceTable = new PieceTable(audio_table_path,record_path,original_audio_path,1000000);
        //audioProject.setPieceTable(pieceTable);
        graph.setTables(bitmapPieceTable, pieceTable);
        //audioProject.setNewRecordFile(projectPaths.audio_original);
        graph.setProjectPaths(projectPaths);
        the_seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time.setText(String.format("%.2f",(double)progress*2/ audioProject.getSampleRate()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //pos_select =the_seeker.getProgress()*2*(audioProject.getSampleRate()/1000);
            }
        });
    }
    public static void setDisplayStream(int buffsize, String file, boolean state, int length,int range) {
        display.setEncoding(range);
        display.setGraphState(state, buffsize, file, length);
    }
    public static Pair<Integer,Integer> getSelectionPoints(){
        return new Pair<>(graph.points.audio_start,graph.points.audio_stop);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backwards:
                scrollView.removeAllViews();
                TimeDomain.backwards backwards = new TimeDomain.backwards();
                TimeDomain.modulation Backwards = backwards::modulate;
                String[] backwards_titles = new String[]{"Volume"};
                int[] backwards_maxes = new int[]{10};
                ModulateControls backwards_view = new ModulateControls(this, backwards_titles, backwards_maxes, new double[]{.1},
                        new String[]{"Volume"}, audioProject, Backwards, Gravity.CENTER,
                        "Backwards Effect", new int[]{10},play_button,seek_n_loader,pieceTable);
                scrollView.addView(backwards_view);
                break;
            case R.id.echo:
                scrollView.removeAllViews();
                TimeDomain.echo echo = new TimeDomain.echo();
                TimeDomain.modulation Echo = echo::modulate;
                int[] echo_maxes = new int[]{10, 10};
                ModulateControls echo_view = new ModulateControls(this,echo_titles, echo_maxes, new double[]{1, 1},
                        new String[]{"S", "D"}, audioProject, Echo, Gravity.CENTER,
                        "Echo Effect", new int[]{5, 6},play_button,seek_n_loader,pieceTable);
                scrollView.addView(echo_view);
                break;
            case R.id.quantize:
                scrollView.removeAllViews();
                TimeDomain.quantized quantized = new TimeDomain.quantized();
                TimeDomain.modulation Quantized = quantized::modulate;
                //TODO rename robotic shit
                String[] robotic_titles = new String[]{"Quantize", "Amplitude"};
                int[] robotic_maxes = new int[]{10, 10};
                ModulateControls robotic = new ModulateControls(this,robotic_titles, robotic_maxes, new double[]{1000, .1},
                        new String[]{"C", "Amp"}, audioProject, Quantized, Gravity.CENTER,
                        "Quantize Audio Sample", new int[]{5, 10},play_button,seek_n_loader,pieceTable);
                scrollView.addView(robotic);
                break;
            case R.id.phaser:
                scrollView.removeAllViews();
                TimeDomain.phaser phaser = new TimeDomain.phaser();
                TimeDomain.modulation Phaser = phaser::modulate;
                int[] phaser_maxes = new int[]{20, 10, 10, 20};
                ModulateControls phaser_view = new ModulateControls(this,phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, 0}, //.1 * Math.PI
                        phaser_quantities, audioProject, Phaser, Gravity.NO_GRAVITY,
                        "Phaser with Sine Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(phaser_view);
                break;
            case R.id.phaser_triangle:
                scrollView.removeAllViews();
                TimeDomain.phaserTriangle phaserTriangle = new TimeDomain.phaserTriangle();
                TimeDomain.modulation PhaserTriangle = phaserTriangle::modulate;
                int[] alien_maxes = new int[]{20, 10, 10, 10};
                ModulateControls phaser_triangle_view = new ModulateControls(this,phaser_titles, alien_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, audioProject, PhaserTriangle, Gravity.NO_GRAVITY,
                        "Phaser with Triangle Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(phaser_triangle_view);
                break;
            case R.id.phaser_square:
                scrollView.removeAllViews();
                TimeDomain.phaserSquare phaserSquare = new TimeDomain.phaserSquare();
                TimeDomain.modulation PhaserSquare = phaserSquare::modulate;
                int[] square_maxes = new int[]{20, 10, 10, 10};
                ModulateControls phaser_square_view = new ModulateControls(this,phaser_titles, square_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, audioProject, PhaserSquare, Gravity.NO_GRAVITY,
                        "Phaser with Square Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(phaser_square_view);
                break;
            case R.id.phaser_saw:
                scrollView.removeAllViews();
                TimeDomain.phaserSaw phaserSaw = new TimeDomain.phaserSaw();
                TimeDomain.modulation PhaserSaw = phaserSaw::modulate;
                int[] saw_maxes = new int[]{20, 10, 10, 10};
                ModulateControls phaser_saw_view = new ModulateControls(this,phaser_titles, saw_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, audioProject, PhaserSaw, Gravity.NO_GRAVITY,
                        "Phaser with Saw Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(phaser_saw_view);
                break;
            case R.id.flanger:
                scrollView.removeAllViews();
                TimeDomain.flanger flanger = new TimeDomain.flanger();
                TimeDomain.modulation Flanger = flanger::modulate;
                int[] flanger_maxes = new int[]{10, 10, 20};
                ModulateControls flanger_view = new ModulateControls(this,flanger_titles, flanger_maxes, new double[]{10, 10, nyquist},
                        new String[]{"∧", "∨", "Hz"}, audioProject, Flanger, Gravity.NO_GRAVITY,
                        "Flanger with Sine Wave", flanger_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(flanger_view);
                break;
            case R.id.flanger_triangle:
                scrollView.removeAllViews();
                TimeDomain.flangerTriangle flangerTriangle = new TimeDomain.flangerTriangle();
                TimeDomain.modulation FlangerTriangle = flangerTriangle::modulate;
                int[] flanger_triangle_maxes = new int[]{10, 10, 20};
                ModulateControls flanger_triangle_view = new ModulateControls(this,flanger_titles, flanger_triangle_maxes, new double[]{10, 10, nyquist},
                        new String[]{"∧", "∨", "Hz"}, audioProject, FlangerTriangle, Gravity.NO_GRAVITY,
                        "Flanger with Triangle Wave", flanger_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(flanger_triangle_view);
                break;
            case R.id.flanger_square:
                scrollView.removeAllViews();
                TimeDomain.flangerSquare flangerSquare = new TimeDomain.flangerSquare();
                TimeDomain.modulation FlangerSquare = flangerSquare::modulate;
                int[] flanger_square_maxes = new int[]{10, 10, 20};
                ModulateControls flanger_square_view = new ModulateControls(this,flanger_titles, flanger_square_maxes, new double[]{10, 10, nyquist},
                        new String[]{"∧", "∨", "Hz"}, audioProject, FlangerSquare, Gravity.NO_GRAVITY,
                        "Flanger with Square Wave", flanger_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(flanger_square_view);
                break;
            case R.id.low_pass:
                scrollView.removeAllViews();
                TimeDomain.lowPass lowPass = new TimeDomain.lowPass();
                TimeDomain.modulation LowPass = lowPass::modulate;
                ModulateControls low_pass_view = new ModulateControls(this,new String[]{"Smoothing"}, new int[]{25}, new double[]{5},
                        new String[]{" "}, audioProject, LowPass, Gravity.CENTER,
                        "Low Pass Filter", flanger_progress,play_button,seek_n_loader,pieceTable);
                scrollView.addView(low_pass_view);
                break;
            case R.id.start_recording:
                audioProject = controls.getCreationData();
                audioProject.setAudioPieceTable(pieceTable);
                audioProject.setProjectPaths(projectPaths);
                nyquist = (audioProject.getSampleRate() / 2) / 20;
                record = new RecordLogic();
                the_seeker.setVisibility(View.GONE);
                time.setVisibility(View.GONE);
                display.setVisibility(View.VISIBLE);
                display.setEncoding(Short.MAX_VALUE*2+1);
                if (pieceTable.byte_length == 0){
                    record.setFileObject(audioProject,projectPaths.audio_original,file_state);
                    display.setGraphState(true, record.buffer_size, projectPaths.audio_original, 1);
                }else{
                    record.setFileObject(audioProject,projectPaths.audio, file_state);
                    display.setGraphState(true, record.buffer_size, projectPaths.audio, 1);}
                file_state = false;
                record.setRecordingState(false);
                record.startRecording(this);
                audioProject.setBufferSize(record.buffer_size);

                //setGraphStream(record.buffer_size, audioProject.getFilePath(),true);
                graph.setGraphState(record.buffer_size,true);

                //setDisplayStream(record.buffer_size, audioProject.getFilePath(),true, 1,Short.MAX_VALUE*2+1);

                record_button.setVisibility(View.INVISIBLE);
                pause_button.setVisibility(View.VISIBLE);
                break;
            case  R.id.pause_recording:

                long length = pieceTable.byte_length;

                record.setRecordingState(true);

                //setGraphStream(record.buffer_size, audioProject.getFilePath(),false);
                graph.setGraphState(record.buffer_size,false);

                display.setEncoding(Short.MAX_VALUE*2+1);
                display.setGraphState(false, record.buffer_size, projectPaths.audio_original, 1);

                graph.catchUp(false);

                display.setVisibility(View.GONE);
                audioProject.setLength((int)length);
                int max = (int) (length/2/ audioProject.getSampleRate()*1000);
                time.setVisibility(View.VISIBLE);
                the_seeker.setMax(max);
                the_seeker.setProgress(max);
                modulations.setVisibility(View.VISIBLE);
                the_seeker.setVisibility(View.VISIBLE);
                play_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.VISIBLE);
                pause_button.setVisibility(View.INVISIBLE);
                record_button.setVisibility(View.VISIBLE);
                break;
            case  R.id.play_recording:
                record.setPieceTable(pieceTable);
                new Thread(() -> {
                    Pair<Integer,Integer> pair;
                    try{
                        pair = getSelectionPoints();
                        record.play_recording(pair.first, pair.second);
                    } catch (NullPointerException e){
                        record.play_recording(0, pieceTable.byte_length);
                    }
                    }).start();
                break;
            case  R.id.stop_recording:
                audioProject.save();
                break;


            }

    }
}

    /*
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

     */
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

