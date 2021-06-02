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
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.modulate.timeDomain;
import com.example.voicemodulation.sequence.PieceTable;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.controls.ModulateControls;
import com.example.voicemodulation.controls.RecordControls;
import com.example.voicemodulation.graph.AudioDisplay;
import com.example.voicemodulation.graph.GraphLogic;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

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
    private HorizontalScrollView testing;
    private HorizontalScrollView graph_scroll;

    private SeekBar the_seeker;
    private RecordControls controls;
    private RecordLogic record;
    private AudioFile noFrag;
    private int pos_select;
    private PieceTable pieceTable;
    private RandomAccessFile jennifer;
    private Stack<Long> audioLength;
    int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};
    private PieceTable bitmapPieceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String namer= Environment.getExternalStorageDirectory().getPath()+"/bitmap";
        String namer1= Environment.getExternalStorageDirectory().getPath()+"/bitmap_piece_table";
        String namer2= Environment.getExternalStorageDirectory().getPath()+"/audio_piece_table";
        audioLength = new Stack<>();
        // Data.getMemory()/x ? instead of static 1MB
        bitmapPieceTable = new PieceTable(namer1,namer,1000000);
        AudioCon.IO_RAF groovy = new AudioCon.IO_RAF(namer);
        jennifer = groovy.getWriteObject(false);
        noFrag = new AudioFile();
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
        testing = findViewById(R.id.fuckFragments);
        display = findViewById(R.id.audio_display);
        modulations = findViewById(R.id.modulations);
        graph = findViewById(R.id.display);
        controls = new RecordControls(this,record_control_titles,record_control_ranges,
                record_control_scales,record_control_quantities,
                record_gravity,record_control_title,record_control_progresses,
                record_controls,graph,seek_n_loader,modulations);
        testing.addView(controls);
        the_seeker = findViewById(R.id.seek);
        pieceTable = new PieceTable(namer2,noFrag.getNewRecordFile(),1000000);
        noFrag.setPieceTable(pieceTable);
        graph.setTables(bitmapPieceTable, pieceTable);
        the_seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time.setText(String.format("%.2f",(double)progress*2/noFrag.getSampleRate()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pos_select =the_seeker.getProgress()*2*(noFrag.getSampleRate()/1000);
            }
        });
    }
    //TODO remove the file param
    public static void setGraphStream(int buffsize, String file, boolean state){
        graph.setGraphState(buffsize,state);
    }
    public static void setDisplayStream(int buffsize, String file, boolean state, int length,int range) {
        //System.out.println("the dynamic range of this encoding is: "+range);
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
                testing.removeAllViews();
                timeDomain.backwards backwards = new timeDomain.backwards();
                timeDomain.modulation Backwards = backwards::modulate;
                String[] backwards_titles = new String[]{"Volume"};
                int[] backwards_maxes = new int[]{10};
                ModulateControls backwards_view = new ModulateControls(this, backwards_titles, backwards_maxes, new double[]{.1},
                        new String[]{"Volume"}, noFrag, Backwards, Gravity.CENTER,
                        "Backwards Effect", new int[]{10},play_button,seek_n_loader,pieceTable);
                testing.addView(backwards_view);
                break;
            case R.id.echo:
                testing.removeAllViews();
                timeDomain.echo echo = new timeDomain.echo();
                timeDomain.modulation Echo = echo::modulate;
                int[] echo_maxes = new int[]{10, 10};
                ModulateControls echo_view = new ModulateControls(this,echo_titles, echo_maxes, new double[]{1, 1},
                        new String[]{"S", "D"}, noFrag, Echo, Gravity.CENTER,
                        "Echo Effect", new int[]{5, 6},play_button,seek_n_loader,pieceTable);
                testing.addView(echo_view);
                break;
            case R.id.quantize:
                testing.removeAllViews();
                timeDomain.quantized quantized = new timeDomain.quantized();
                timeDomain.modulation Quantized = quantized::modulate;
                //TODO rename robotic shit
                String[] robotic_titles = new String[]{"Quantize", "Amplitude"};
                int[] robotic_maxes = new int[]{10, 10};
                ModulateControls robotic = new ModulateControls(this,robotic_titles, robotic_maxes, new double[]{1000, .1},
                        new String[]{"C", "Amp"}, noFrag, Quantized, Gravity.CENTER,
                        "Quantize Audio Sample", new int[]{5, 10},play_button,seek_n_loader,pieceTable);
                testing.addView(robotic);
                break;
            case R.id.phaser:
                testing.removeAllViews();
                timeDomain.phaser phaser = new timeDomain.phaser();
                timeDomain.modulation Phaser = phaser::modulate;
                int[] phaser_maxes = new int[]{20, 10, 10, 20};
                ModulateControls phaser_view = new ModulateControls(this,phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, 0}, //.1 * Math.PI
                        phaser_quantities, noFrag, Phaser, Gravity.NO_GRAVITY,
                        "Phaser with Sine Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(phaser_view);
                break;
            case R.id.phaser_triangle:
                testing.removeAllViews();
                timeDomain.phaserTriangle phaserTriangle = new timeDomain.phaserTriangle();
                timeDomain.modulation PhaserTriangle = phaserTriangle::modulate;
                int[] alien_maxes = new int[]{20, 10, 10, 10};
                ModulateControls phaser_triangle_view = new ModulateControls(this,phaser_titles, alien_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, noFrag, PhaserTriangle, Gravity.NO_GRAVITY,
                        "Phaser with Triangle Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(phaser_triangle_view);
                break;
            case R.id.phaser_square:
                testing.removeAllViews();
                timeDomain.phaserSquare phaserSquare = new timeDomain.phaserSquare();
                timeDomain.modulation PhaserSquare = phaserSquare::modulate;
                int[] square_maxes = new int[]{20, 10, 10, 10};
                ModulateControls phaser_square_view = new ModulateControls(this,phaser_titles, square_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, noFrag, PhaserSquare, Gravity.NO_GRAVITY,
                        "Phaser with Square Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(phaser_square_view);
                break;
            case R.id.phaser_saw:
                testing.removeAllViews();
                timeDomain.phaserSaw phaserSaw = new timeDomain.phaserSaw();
                timeDomain.modulation PhaserSaw = phaserSaw::modulate;
                int[] saw_maxes = new int[]{20, 10, 10, 10};
                ModulateControls phaser_saw_view = new ModulateControls(this,phaser_titles, saw_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, noFrag, PhaserSaw, Gravity.NO_GRAVITY,
                        "Phaser with Saw Wave", phaser_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(phaser_saw_view);
                break;
            case R.id.flanger:
                testing.removeAllViews();
                timeDomain.flanger flanger = new timeDomain.flanger();
                timeDomain.modulation Flanger = flanger::modulate;
                int[] flanger_maxes = new int[]{10, 10, 20};
                ModulateControls flanger_view = new ModulateControls(this,flanger_titles, flanger_maxes, new double[]{10, 10, nyquist},
                        new String[]{"∧", "∨", "Hz"}, noFrag, Flanger, Gravity.NO_GRAVITY,
                        "Flanger with Sine Wave", flanger_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(flanger_view);
                break;
            case R.id.flanger_triangle:
                testing.removeAllViews();
                timeDomain.flangerTriangle flangerTriangle = new timeDomain.flangerTriangle();
                timeDomain.modulation FlangerTriangle = flangerTriangle::modulate;
                int[] flanger_triangle_maxes = new int[]{10, 10, 20};
                ModulateControls flanger_triangle_view = new ModulateControls(this,flanger_titles, flanger_triangle_maxes, new double[]{10, 10, nyquist},
                        new String[]{"∧", "∨", "Hz"}, noFrag, FlangerTriangle, Gravity.NO_GRAVITY,
                        "Flanger with Triangle Wave", flanger_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(flanger_triangle_view);
                break;
            case R.id.flanger_square:
                testing.removeAllViews();
                timeDomain.flangerSquare flangerSquare = new timeDomain.flangerSquare();
                timeDomain.modulation FlangerSquare = flangerSquare::modulate;
                int[] flanger_square_maxes = new int[]{10, 10, 20};
                ModulateControls flanger_square_view = new ModulateControls(this,flanger_titles, flanger_square_maxes, new double[]{10, 10, nyquist},
                        new String[]{"∧", "∨", "Hz"}, noFrag, FlangerSquare, Gravity.NO_GRAVITY,
                        "Flanger with Square Wave", flanger_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(flanger_square_view);
                break;
            case R.id.low_pass:
                testing.removeAllViews();
                timeDomain.lowPass lowPass = new timeDomain.lowPass();
                timeDomain.modulation LowPass = lowPass::modulate;
                ModulateControls low_pass_view = new ModulateControls(this,new String[]{"Smoothing"}, new int[]{25}, new double[]{5},
                        new String[]{" "}, noFrag, LowPass, Gravity.CENTER,
                        "Low Pass Filter", flanger_progress,play_button,seek_n_loader,pieceTable);
                testing.addView(low_pass_view);
                break;
            case R.id.start_recording:
                noFrag = controls.getCreationData();
                nyquist = (noFrag.getSampleRate() / 2) / 20;
                record = new RecordLogic();
                the_seeker.setVisibility(View.GONE);
                time.setVisibility(View.GONE);
                display.setVisibility(View.VISIBLE);
                record.setFileObject(noFrag, file_state);
                file_state = false;
                record.setRecordingState(false);
                record.startRecording();
                noFrag.setBufferSize(record.buffer_size);
                setGraphStream(record.buffer_size,noFrag.getFilePath(),true);
                setDisplayStream(record.buffer_size,noFrag.getFilePath(),true, 1,Short.MAX_VALUE*2+1);
                record_button.setVisibility(View.INVISIBLE);
                pause_button.setVisibility(View.VISIBLE);
                break;
            case  R.id.pause_recording:
                AudioCon.IO_RAF readOnly = new AudioCon.IO_RAF(noFrag.getNewRecordFile());
                RandomAccessFile f = readOnly.getReadObject();
                long length =0;
                long blength=0;
                record.setRecordingState(true);
                try {
                    length= f.length();
                    audioLength.push(length);
                    blength = jennifer.length();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (pieceTable._text_len == 0){
                    pieceTable.add_original((int) length);
                    bitmapPieceTable.add_original((int) blength);
               }else{
                    pieceTable.add((int) (length-pieceTable._text_len), pieceTable._text_len);
                    bitmapPieceTable.print_pieces();
                    bitmapPieceTable.add((int)blength-bitmapPieceTable._text_len, bitmapPieceTable._text_len);
                    audioLength.remove(0);
                }
                graph.catchUp(false);
                graph.setTables(bitmapPieceTable, pieceTable);
                setDisplayStream(record.buffer_size,noFrag.getNewRecordFile(),false, 1,Short.MAX_VALUE*2+1);
                display.setVisibility(View.GONE);
                noFrag.setLength((int)length);
                int max = (int) (length/2/noFrag.getSampleRate()*1000);
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
                Pair<Integer,Integer> pair = getSelectionPoints();
                pos_select=the_seeker.getProgress()*2*(noFrag.getSampleRate()/1000);
                new Thread(() -> {
                    //record.play_recording(pair.first, pair.second);
                    record.play_recording(0, (int) pieceTable._text_len);
                    }).start();
                break;
            case  R.id.stop_recording:
                noFrag.save();
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

