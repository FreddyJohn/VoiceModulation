package com.example.voicemodulation.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.util.Convert;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

/*
the reason we need this class is because we want to change the dataInputStream of
GraphLogic depending on the context of MainActivity
for example: while the user is recording audio this class should display
the real time audio
    this is accomplished in RControls, RControls makes its own instance of GraphicLogic passing it the status,
    the name of its dataOutputStream, and the size of the data buffer determined by AudioRecord android side

   However, this data stream is being used by another graph at a different scale within the current code of GraphLogic.
   GraphLogic needs to maintain a location in memory as it does with data.0
   GraphLogic will be able to read and write byte buffers to any location within this place in memory
        so will RecordLogic
        and so will ModulateLogic
   with these requirements, GraphLogic should be it's own dedicated class

Now what should AudioDisplay be responsible for?
AudioDisplay should just display any operations that involves data of n encoding being written or read in real time

Now who and in where will AudioDisplay be called?

 */
/*TODO you listen whore -> put this view back in GraphLogic so you only have the one data buffer
    also, add a seek bar then dynamically display either this view or the seekbar based on context of MainActivity

 */
public class AudioDisplay extends View {
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private boolean graphState = false;
    private DataInputStream jane;
    private int bufferSize;
    private LinkedList<Short> data;
    private int graph_pos=0;
    public AudioDisplay(Context context) {
        super(context);
        init(context, null);
    }

    public AudioDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AudioDisplay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public AudioDisplay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attributeSet) //,int graphID)
    {
        LinearLayout.LayoutParams view_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        view_params.gravity = Gravity.CENTER;
        data = new LinkedList<>();
        paint = new Paint();
        paint.setColor(Color.RED);
        pixel_density = Resources.getSystem().getDisplayMetrics().density;
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(pixel_density);

    }
    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        view_width = width;
        view_height = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (graphState) {
            startGraphing(canvas);
        }
    }
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        setMeasuredDimension(width, height);
    }
    public void setGraphState(boolean state, int buffer_size,String in_file) { //TODO we need to know about SeekBar position
        //TODO there are two different ways in which this class will be used
        //  1.) display input stream from n offset of memory location
        //  2.) display the entire stream
        //  both of these conditions can be described with one variable -> the offset 1.) = n , 2.) = 0
        this.graphState=state;
        this.bufferSize=buffer_size;
        try {
            //File i = new File(in_file);
            this.jane = new DataInputStream(new FileInputStream(in_file));
            AudioCon.IO_RAF con = new AudioCon.IO_RAF(in_file);
            RandomAccessFile f = con.getReadObject();
            long length = f.length();
            this.jane = new DataInputStream(new FileInputStream(in_file));
            System.out.println("Length in Bytes: "+ f.length());
            System.out.println("Bytes skipped: "+jane.skipBytes((int) length));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        invalidate(); }
    public void startGraphing(Canvas canvas) {
        byte[] buffer = new byte[bufferSize];
        try {
            jane.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FAILED TO READ BUFFER");
        }
        short[] chunk = Convert.getShortsFromBytes(buffer);
        for(int i=0; i<chunk.length; i++) {
            graph_pos += .1;
            canvas.drawLine(graph_pos, view_height / 2, graph_pos, (view_height / 2) - chunk[i] * (view_height / 65535), paint);
            canvas.translate(+1, 0);
        }
        invalidate();
    }
}
