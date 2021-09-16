package com.example.voicemodulation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.example.voicemodulation.audio.Export;
import com.example.voicemodulation.audio.RecordLogic;
import com.example.voicemodulation.controls.ModulateControls;
import com.example.voicemodulation.database.AppDatabase;
import com.example.voicemodulation.database.ProjectDao;
import com.example.voicemodulation.database.project.AudioData;
import com.example.voicemodulation.database.project.Paths;
import com.example.voicemodulation.database.project.Project;

import com.example.voicemodulation.graph.AudioDisplay;
import com.example.voicemodulation.graph.GraphLogic;
import com.example.voicemodulation.structures.Structure;
import com.example.voicemodulation.signal.Modulation;
import com.example.voicemodulation.util.FileUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;


    private double nyquist = 0;

    private String[] phaser_titles; //= new String[]{getString(R.string.freq), getString(R.string.carrier_amp), getString(R.string.modulator_amp), getString(R.string.title_theta)};
    private String[] phaser_quantities; //= new String[]{getString(R.string.hz), getString(R.string.amp), getString(R.string.amp), getString(R.string.theta)};
    private final int[] phaser_progress = new int[]{1, 10, 10, 0};
    private final int[] phaser_maxes = new int[]{20, 10, 10, 10};
    private String[] flanger_titles; //= new String[]{getString(R.string.delay), getString(R.string.amplitude), getString(R.string.wet), getString(R.string.freq)};
    private String[] flanger_quantities; //= new String[]{getString(R.string.s), getString(R.string.amp), " '", getString(R.string.hz)};
    private final int[] flanger_maxes = new int[]{10, 20, 10, 20};
    private final double[] flanger_scales = new double[]{.01, 1, 1, 1};
    private final int[] flanger_progress = new int[]{5, 8, 4, 1};

    private String[] echo_titles; //= new String[]{getString(R.string.delay)};
    private final int[] record_control_ranges = new int[]{10, 10};
    private final int[] record_control_scales = new int[]{4800, 4800};
    private final int[] record_control_progresses = new int[]{10, 10};
    /*
    private final String[] record_control_titles = new String[]{"Playback Rate", "Sample Rate", "Format", "Channels", "Encoding"};
    private final String[] record_control_quantities = new String[]{"Hz", "Hz", null, null, null};
    private final int[] record_control_ranges = new int[]{10, 10, 2, 1, 1};
    private final int[] record_control_scales = new int[]{4800, 4800, 1, 1, 1};
    private final int[] record_control_progresses = new int[]{10, 10, 2, 0, 1};
     */
    private ImageButton play_button, stop_button,
            record_button, pause_button;
    private TextView time;
    private static AudioDisplay display;
    private static GraphLogic graph;
    private HorizontalScrollView modulations;
    private FrameLayout projectInfo;
    private HorizontalScrollView scrollView;
    //private RecordControls controls;
    private RecordLogic record;
    private AudioData audioData;
    public static Structure audioPieceTable;
    private Structure bitmapPieceTable;

    int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private Paths projectPaths;
    private static ArrayList<Thread> threadList;
    private boolean storagePermission;
    private boolean recordPermission;
    private ProjectDao userDao;
    private Project newProject;
    private TextView memory;
    private TextView frequency;
    private View v;
    private boolean recordingState;
    private ImageButton export_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        graph = findViewById(R.id.display);

        threadList = new ArrayList<>();

        echo_titles = new String[]{getString(R.string.delay)};
        //String[] record_control_titles = new String[]{getString(R.string.playback_rate), getString(R.string.sample_rate)};
        //String[] record_control_quantities = new String[]{getString(R.string.hz), getString(R.string.hz)};
        flanger_titles = new String[]{getString(R.string.delay), getString(R.string.amplitude), getString(R.string.wet), getString(R.string.freq)};
        flanger_quantities = new String[]{getString(R.string.s), getString(R.string.amp), " '", getString(R.string.hz)};
        phaser_titles = new String[]{getString(R.string.freq), getString(R.string.carrier_amp), getString(R.string.modulator_amp), getString(R.string.title_theta)};
        phaser_quantities = new String[]{getString(R.string.hz), getString(R.string.amp), getString(R.string.amp), getString(R.string.theta)};

        //FrameLayout record_controls = findViewById(R.id.record_controls);
        record_button = findViewById(R.id.start_recording);
        play_button = findViewById(R.id.play_recording);
        pause_button = findViewById(R.id.pause_recording);
        stop_button = findViewById(R.id.stop_recording);
        export_button = findViewById(R.id.export);

        play_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        pause_button.setVisibility(View.INVISIBLE);

        time = findViewById(R.id.time);
        memory = findViewById(R.id.memory);
        frequency = findViewById(R.id.freq);

        projectInfo = findViewById(R.id.info);
        scrollView = findViewById(R.id.viewHolder);
        display = findViewById(R.id.audio_display);
        modulations = findViewById(R.id.modulations);

        //int record_gravity = Gravity.NO_GRAVITY;
        //String record_control_title = getString(R.string.record_controls);
        /*
        controls = new RecordControls(this, record_control_titles, record_control_ranges,
                record_control_scales, record_control_quantities,
                record_gravity, record_control_title, record_control_progresses,
                record_controls, graph, projectInfo, modulations);

         */
        //scrollView.addView(controls);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").allowMainThreadQueries().build();
        userDao = db.projectDao();
        newProject = new Project();
        newProject.audioData = new AudioData();
        //graph.requestLayout();
        //graph.postInvalidate();
        //newProject.audioData.width = (int) graph.view_width;
        //newProject.audioData.height = (int) graph.view_height;
        System.out.println("in on create width="+graph.view_width+" height="+graph.view_height);
        System.out.println("in on create width="+graph.getMeasuredWidth()+" height="+graph.getMeasuredHeight());

        stop_button.setOnLongClickListener(v -> {
            stop_button.setVisibility(View.INVISIBLE);
            export_button.setVisibility(View.VISIBLE);
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View view = layoutInflater.inflate(R.layout.name_project, null);
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setView(view);
            final EditText dialog = (EditText) view.findViewById(R.id.userInputDialog);
            alert.setCancelable(false).setPositiveButton(getString(R.string.export), (dialogBox, id) -> {
                Export.format(newProject, audioPieceTable,Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_MUSIC+"/"+dialog.getText().toString());
                Toast.makeText(this,"Project saved to music folder",Toast.LENGTH_SHORT).show();
                stop_button.setVisibility(View.VISIBLE);
                export_button.setVisibility(View.INVISIBLE);
            }).setNegativeButton(getString(R.string.cancel), (dialogBox, id) ->{
                stop_button.setVisibility(View.VISIBLE);
                export_button.setVisibility(View.INVISIBLE);
                dialogBox.cancel();
                });
            AlertDialog alertDialogAndroid = alert.create();
            alertDialogAndroid.show();
            return false;
        });
        initializeProjectData();
        modulations.setVisibility(View.VISIBLE);
        Modulation.flanger flanger = new Modulation.flanger();
        ModulateControls flanger_view = new ModulateControls(this, flanger_titles, flanger_maxes, flanger_scales,
                flanger_quantities, newProject, flanger,
                getString(R.string.flanger_sine_title), flanger_progress, play_button, projectInfo);
        scrollView.addView(flanger_view);
    }

    public static void setDisplayStream(int buffsize, String file, boolean state, int length, int range) {
        display.setEncoding(range);
        display.setGraphState(state, buffsize, file, length);
    }

    public static Pair<Integer, Integer> getSelectionPoints() {
        if (graph.points != null) {
            return new Pair<>(graph.points.audio_start, graph.points.audio_stop);
        }
        return new Pair<>(0, audioPieceTable.byte_length);
    }

    public static void addThread(Thread thread) {
        threadList.add(thread);
    }

    @Override
    public void onClick(View v) {
        this.v = v;
        int vId = v.getId();
        if (hasPermissions(this, PERMISSIONS)) {
            if (projectPaths == null) {
                initializeProjectStructures();
            }
            if (vId == R.id.undo) {
                if (!recordingState) {
                    if (audioPieceTable.undo() & bitmapPieceTable.undo()) {
                        Toast.makeText(this, getString(R.string.undo_pressed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.nothing_to_undo), Toast.LENGTH_SHORT).show();
                    }
                    graph.populateProject();
                }
            } else if (vId == R.id.redo) {
                if (!recordingState) {
                    if (audioPieceTable.redo() & bitmapPieceTable.redo()) {
                        Toast.makeText(this, getString(R.string.redo_pressed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.nothing_to_redo), Toast.LENGTH_SHORT).show();
                    }
                    graph.populateProject();
                }
            }
            else if (vId == R.id.backwards) {
                scrollView.removeAllViews();
                Modulation.backwards backwards = new Modulation.backwards();
                String[] backwards_titles = new String[]{getString(R.string.volume)};
                int[] backwards_maxes = new int[]{10};
                ModulateControls backwards_view = new ModulateControls(this, backwards_titles, backwards_maxes, new double[]{.1},
                        new String[]{getString(R.string.volume)}, newProject, backwards,
                        getString(R.string.backwards_title), new int[]{10}, play_button, projectInfo);
                scrollView.addView(backwards_view);
            } else if (vId == R.id.echo) {
                scrollView.removeAllViews();
                Modulation.echo echo = new Modulation.echo();
                int[] echo_maxes = new int[]{20};
                ModulateControls echo_view = new ModulateControls(this, echo_titles, echo_maxes, new double[]{.02},
                        new String[]{getString(R.string.s)}, newProject, echo,
                        getString(R.string.echo_title), new int[]{2}, play_button, projectInfo);
                scrollView.addView(echo_view);
            } else if (vId == R.id.quantize) {
                scrollView.removeAllViews();
                Modulation.quantized quantized = new Modulation.quantized();
                String[] quantized_titles = new String[]{getString(R.string.quantize), getString(R.string.amplitude)};
                int[] quantized_maxes = new int[]{10, 10};
                ModulateControls quantize_view = new ModulateControls(this, quantized_titles, quantized_maxes, new double[]{1000, .1},
                        new String[]{"C", "Amp"}, newProject, quantized,
                        getString(R.string.quantize_title), new int[]{5, 10}, play_button, projectInfo);
                scrollView.addView(quantize_view);
            } else if (vId == R.id.phaser) {
                scrollView.removeAllViews();
                Modulation.phaser phaser = new Modulation.phaser();
                ModulateControls phaser_view = new ModulateControls(this, phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI}, // was 0 idk why
                        phaser_quantities, newProject, phaser,
                        getString(R.string.phaser_sine_title), phaser_progress, play_button, projectInfo);
                scrollView.addView(phaser_view);
            } else if (vId == R.id.phaser_triangle) {
                scrollView.removeAllViews();
                Modulation.modulation PhaserTriangle = new Modulation.phaserTriangle();
                ModulateControls phaser_triangle_view = new ModulateControls(this, phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, newProject, PhaserTriangle,
                        getString(R.string.phaser_triangle_title), phaser_progress, play_button, projectInfo);
                scrollView.addView(phaser_triangle_view);
            } else if (vId == R.id.phaser_square) {
                scrollView.removeAllViews();
                Modulation.phaserSquare phaserSquare = new Modulation.phaserSquare();
                ModulateControls phaser_square_view = new ModulateControls(this, phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, newProject, phaserSquare,
                        getString(R.string.phaser_square_title), phaser_progress, play_button, projectInfo);
                scrollView.addView(phaser_square_view);
            } else if (vId == R.id.phaser_saw) {
                scrollView.removeAllViews();
                Modulation.phaserSaw phaserSaw = new Modulation.phaserSaw();
                ModulateControls phaser_saw_view = new ModulateControls(this, phaser_titles, phaser_maxes, new double[]{nyquist, .1, .1, .1 * Math.PI},
                        phaser_quantities, newProject, phaserSaw,
                        getString(R.string.phasor_saw_title), phaser_progress, play_button, projectInfo);
                scrollView.addView(phaser_saw_view);
            } else if (vId == R.id.flanger) {
                scrollView.removeAllViews();
                Modulation.flanger flanger = new Modulation.flanger();
                ModulateControls flanger_view = new ModulateControls(this, flanger_titles, flanger_maxes, flanger_scales,
                        flanger_quantities, newProject, flanger,
                        getString(R.string.flanger_sine_title), flanger_progress, play_button, projectInfo);
                scrollView.addView(flanger_view);
            } else if (vId == R.id.flanger_triangle) {
                scrollView.removeAllViews();
                Modulation.flangerTriangle flangerTriangle = new Modulation.flangerTriangle();
                ModulateControls flanger_triangle_view = new ModulateControls(this, flanger_titles, flanger_maxes, flanger_scales,
                        flanger_quantities, newProject, flangerTriangle,
                        getString(R.string.flanger_triangle_title), flanger_progress, play_button, projectInfo);
                scrollView.addView(flanger_triangle_view);
            } else if (vId == R.id.flanger_square) {
                scrollView.removeAllViews();
                Modulation.flangerSquare flangerSquare = new Modulation.flangerSquare();
                ModulateControls flanger_square_view = new ModulateControls(this, flanger_titles, flanger_maxes, flanger_scales,
                        flanger_quantities, newProject, flangerSquare,
                        getString(R.string.flanger_square_title), flanger_progress, play_button, projectInfo);
                scrollView.addView(flanger_square_view);
            } else if (vId == R.id.flanger_saw) {
                scrollView.removeAllViews();
                Modulation.flangerSaw flangerSaw = new Modulation.flangerSaw();
                ModulateControls flanger_saw_view = new ModulateControls(this, flanger_titles, flanger_maxes, flanger_scales,
                        flanger_quantities, newProject, flangerSaw,
                        getString(R.string.flanger_saw_title), flanger_progress, play_button, projectInfo);
                scrollView.addView(flanger_saw_view);
            } else if (vId == R.id.low_pass) {
                scrollView.removeAllViews();
                Modulation.lowPass lowPass = new Modulation.lowPass();
                ModulateControls low_pass_view = new ModulateControls(this, new String[]{getString(R.string.smoothing)}, new int[]{10}, new double[]{20},
                        new String[]{" "}, newProject, lowPass,
                        getString(R.string.low_pass_title), flanger_progress, play_button, projectInfo);
                scrollView.addView(low_pass_view);
            } else if (vId == R.id.volume) {
                scrollView.removeAllViews();
                Modulation.amplitude amp = new Modulation.amplitude();
                ModulateControls amp_view = new ModulateControls(this, new String[]{getString(R.string.volume)}, new int[]{10}, new double[]{1},
                        new String[]{" "}, newProject, amp,
                        getString(R.string.amplitude), flanger_progress, play_button, projectInfo);
                scrollView.addView(amp_view);
            } else if (vId == R.id.robot) {
                scrollView.removeAllViews();
                Modulation.robot robot = new Modulation.robot();
                String[] robotic_titles = new String[]{getString(R.string.robotness)};
                int[] robotic_maxes = new int[]{20};
                ModulateControls robotic = new ModulateControls(this, robotic_titles, robotic_maxes, new double[]{.1},
                        new String[]{" "}, newProject, robot,
                        getString(R.string.robot_title), new int[]{1}, play_button, projectInfo);
                scrollView.addView(robotic);
            } else if (vId == R.id.start_recording) {
                /*
                if (newProject.audioData == null) {
                    newProject.audioData = controls.getCreationData();
                    audioData = newProject.audioData;
                    userDao.insertProject(newProject);
                }
                newProject.audioData = newProject.audioData != null ? newProject.audioData : controls.getCreationData();
                audioData = newProject.audioData;
                 */

                //nyquist = (audioData.sample_rate / 2.0) / 20;
                /*
                AudioConnect.IO_RAF funky = new AudioConnect.IO_RAF(projectPaths.audio_original);
                RandomAccessFile sanity_check = funky.getWriteObject();
                try {
                    System.out.println("no way this will also be zero " + sanity_check.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 */
                //record = new RecordLogic();

                time.setVisibility(View.GONE);
                memory.setVisibility(View.GONE);
                frequency.setVisibility(View.GONE);
                display.setVisibility(View.VISIBLE);
                display.setEncoding(Short.MAX_VALUE * 2 + 1);

                if (audioPieceTable.byte_length == 0) {
                    System.out.println("the length is 0 so we will write an original");
                    record.setFileObject(audioData, projectPaths.audio_original);
                    display.setGraphState(true, record.buffer_size, projectPaths.audio_original, 1);
                } else {
                    if (audioPieceTable.hasEdits()) {
                        System.out.println("imported a project with only an original piece");
                        graph.setProjectPaths(newProject.paths);
                    }
                    System.out.println("the length is not zero so we will write an addition");
                    record.setFileObject(audioData, projectPaths.audio);
                    display.setGraphState(true, record.buffer_size, projectPaths.audio, 1);
                }
                record.setFileData(newProject.audioData, newProject.paths.modulation);
                record.isPaused(false);
                record.startRecording();
                recordingState = true;
                userDao.insertBufferSize(newProject, record.buffer_size);
                graph.setGraphState(record.buffer_size, true);
                //userDao.updateColumnWidth((int) graph.view_height,newProject.uid );
                record_button.setVisibility(View.INVISIBLE);
                pause_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.INVISIBLE);
                play_button.setVisibility(View.INVISIBLE);
            } else if (vId == R.id.pause_recording) {
                record.isPaused(true);
                recordingState = false;
                graph.setGraphState(record.buffer_size, false);

                long length = audioPieceTable.byte_length;

                audioPieceTable.setUnit(2);  // short is two bytes
                bitmapPieceTable.setUnit(4); // int is four bytes

                display.setEncoding(Short.MAX_VALUE * 2 + 1);
                display.setGraphState(false, record.buffer_size, projectPaths.audio, 1);
                graph.catchUp(false);
                display.setVisibility(View.GONE);
                long max = (long) (length / 2.0 / audioData.sample_rate);


                time.setVisibility(View.VISIBLE);
                memory.setVisibility(View.VISIBLE);
                frequency.setVisibility(View.VISIBLE);
                frequency.setText(audioData.sample_rate + " Hz");
                memory.setText(FileUtil.formatMemory(length));
                time.setText(FileUtil.formatTime(max));
                play_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.VISIBLE);
                pause_button.setVisibility(View.INVISIBLE);
                record_button.setVisibility(View.VISIBLE);
                modulations.setVisibility(View.VISIBLE);

            } else if (vId == R.id.play_recording) {
                record.setPieceTable(audioPieceTable.getMostRecent());
                new Thread(() -> {
                    Pair<Integer, Integer> points;
                    try {
                        points = getSelectionPoints();
                        record.play_recording(points.first, points.second);
                    } catch (NullPointerException e) {
                        record.play_recording(0, audioPieceTable.byte_length);
                    }
                }).start();
            } else if (vId == R.id.stop_recording) {
                for (Thread thread : threadList) {
                    if (thread.isAlive()) {
                        thread.interrupt();
                    }
                }
            } else if (vId == R.id.projects) {
                displayProjectList(v);
            } else if (vId == R.id.save){
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View view = layoutInflater.inflate(R.layout.name_project, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setView(view);
                final EditText dialog = (EditText) view.findViewById(R.id.userInputDialog);
                alert.setCancelable(false).setPositiveButton(R.string.save, (dialogBox, id) -> {
                    userDao.updateProjectName(dialog.getText().toString(), newProject);
                }).setNegativeButton(getString(R.string.cancel), (dialogBox, id) ->{
                    dialogBox.cancel();
                });
                AlertDialog alertDialogAndroid = alert.create();
                alertDialogAndroid.show();
            }
        } else if (!recordPermission | !storagePermission) {
            getPermissions();
        }
    }

    private void initializeProjectData() {
        //newProject = new Project();
        newProject.audioData.sample_rate = 44100;
        newProject.audioData.playback_rate = 44100;
        newProject.audioData.format = ".wav";
        newProject.audioData.num_channels_in = AudioFormat.CHANNEL_IN_MONO;
        newProject.audioData.num_channels_out = AudioFormat.CHANNEL_OUT_MONO;
        newProject.audioData.bit_depth = AudioFormat.ENCODING_PCM_16BIT;
        audioData = newProject.audioData;
        nyquist = (audioData.sample_rate / 2.0) / 20;
    }

    private void initializeProjectStructures() {
        projectPaths = FileUtil.createNewProjectPaths(this,
                new ArrayList<String>() {{
                    add("bitmap");
                    add("bitmap_piece_table");
                    add("audio_piece_table");
                    add("original_audio_piece");
                    add("original_bitmap_piece");
                    add("rec.pcm");
                    add("mod.pcm");
                    add("bitmap_edits_stack");
                    add("audio_edits_stack");
                    add("bitmap_remove_stack");
                    add("audio_remove_stack");
                }});
        newProject.paths = projectPaths;
        newProject.project_name = "AutoSave " + userDao.getUid();
        bitmapPieceTable = new Structure(projectPaths.bitmap_table, projectPaths.bitmap,
                projectPaths.bitmap_original, projectPaths.bitmap_edits, projectPaths.bitmap_remove_stack);
        audioPieceTable = new Structure(projectPaths.audio_table, projectPaths.audio,
                projectPaths.audio_original, projectPaths.audio_edits, projectPaths.audio_remove_stack);
        record = new RecordLogic();
        graph.setTables(bitmapPieceTable, audioPieceTable);
        graph.setOriginalPaths(projectPaths);
        record.setFileObject(audioData, projectPaths.audio_original);
        record.setFileData(newProject.audioData, newProject.paths.modulation);
        record.setPieceTable(audioPieceTable);
        //newProject.audioData.width = (int) graph.view_width;
        //newProject.audioData.height = (int)graph.view_height;
        System.out.println("view width="+graph.view_width+" view height="+graph.view_height);
        userDao.insertProject(newProject);

    }

    private void displayProjectList(View view) {
        if (!recordingState) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            List<String> title_list = userDao.getProjectNames();
            if (title_list != null) {
                for (CharSequence title : title_list) {
                    SubMenu savedProject = popupMenu.getMenu().addSubMenu(title);
                    addListeners(savedProject,title);
                }
            }
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_main, popupMenu.getMenu());
            popupMenu.show();
        }
    }

    private void addListeners(SubMenu savedProject, CharSequence title) {
        savedProject.add(getString(R.string.open)).setOnMenuItemClickListener(item -> {
            Project project = userDao.getProjectFromName((String) title);
            if (project.uid != newProject.uid) {
                newProject = project;
                audioData = newProject.audioData;
                projectPaths = newProject.paths;

                bitmapPieceTable = new Structure(projectPaths.bitmap_table, projectPaths.bitmap,
                        projectPaths.bitmap_original, projectPaths.bitmap_edits, projectPaths.bitmap_remove_stack);
                audioPieceTable = new Structure(projectPaths.audio_table, projectPaths.audio,
                        projectPaths.audio_original, projectPaths.audio_edits, projectPaths.audio_remove_stack);

                record = new RecordLogic();
                nyquist = (audioData.sample_rate / 2.0) / 20;
                record.setFileData(newProject.audioData, newProject.paths.modulation);
                record.setPieceTable(audioPieceTable);

                graph = findViewById(R.id.display);
                graph.setTables(bitmapPieceTable, audioPieceTable);
                //graph.setDimensions(newProject.audioData.width,newProject.audioData.height);
                if (audioPieceTable.hasEdits()) {
                    graph.setOriginalPaths(newProject.paths);
                } else {
                    graph.setProjectPaths(newProject.paths);
                }
                graph.buffer_size = newProject.audioData.buffer_size;
                graph.populateProject();
                int max = audioPieceTable.byte_length / 2 / audioData.sample_rate * 1000;
                time.setVisibility(View.VISIBLE);
                memory.setVisibility(View.VISIBLE);
                frequency.setVisibility(View.VISIBLE);
                modulations.setVisibility(View.VISIBLE);
                play_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.VISIBLE);
                record_button.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(this, getString(R.string.already_open),Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        savedProject.add(getString(R.string.delete)).setOnMenuItemClickListener(item -> {

            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        userDao.deleteProject((String)savedProject.getItem().getTitle());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure?").setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();
            return false;
        });
        savedProject.add(getString(R.string.rename)).setOnMenuItemClickListener(item -> {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View view = layoutInflater.inflate(R.layout.name_project, null);
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setView(view);
            final EditText dialog = (EditText) view.findViewById(R.id.userInputDialog);
            alert.setCancelable(false).setPositiveButton(R.string.rename, (dialogBox, id) -> {
                userDao.updateProjectName(dialog.getText().toString(), newProject);
            }).setNegativeButton(getString(R.string.cancel), (dialogBox, id) ->{
                dialogBox.cancel();
            });
            AlertDialog alertDialogAndroid = alert.create();
            alertDialogAndroid.show();
            return false;
        });
    }

    public void getPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }
    }

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
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == -1) {
                storagePermission = false;
            } else if (grantResults[0] == 0) {
                storagePermission = true;
            }
            if (grantResults[2] == -1) {
                recordPermission = false;
            } else if (grantResults[2] == 0) {
                recordPermission = true;
            }
        }
        if (recordPermission & storagePermission) {
            v.callOnClick();
        }
    }
}
