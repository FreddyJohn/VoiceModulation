package com.example.voicemodulation.graph;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.example.voicemodulation.MainActivity;
import com.example.voicemodulation.R;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.util.Convert;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
public class GraphLogic extends View {
    private float pixel_density;
    private SeekBar seeker;
    private Paint paint;
    private float view_height;
    private float view_width;
    //private float this_thing;
    private boolean graphState = false;
    private DataInputStream jill;
    private DataInputStream jane;
    private int bufferSize;
    private Bitmap mExtraBitmap;
    private Canvas mExtraCanvas;
    private int iter;
    private LinkedList<Short> data;
    private int graph_pos=0;
    private boolean liveAudioState =false;
    private float testing;

    public GraphLogic(Context context) {
        super(context);
        init(context, null);
    }
    public GraphLogic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public GraphLogic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    public void init(Context context, AttributeSet attributeSet) //,int graphID)
    {
        data = new LinkedList<>();
        paint = new Paint();
        paint.setColor(Color.RED);
        pixel_density = Resources.getSystem().getDisplayMetrics().density;
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7-.05f/pixel_density);
        try {
            //TODO fix this shit
            String name = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
            //File i = new File(name);
            this.jill = new DataInputStream(new FileInputStream(name));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
             /*TODO
                do not think of this as being where the drawable will take place
                from direct input from the user.
                Rather, this is where you update instance variables that are used elsewhere for drawing operations
                based on the user action event
             */
                case MotionEvent.ACTION_DOWN:
                    System.out.println(event.getX());
                    break;
            }

            return false;
        });


    }
    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (oldWidth==0 && oldHeight==0)
        {
            mExtraBitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            mExtraCanvas = new Canvas(mExtraBitmap);
            view_width = width;
            view_height = height;
        } else {
            view_width = oldWidth;
            view_height = oldHeight;
        }


    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mExtraBitmap, 0, 0, null);
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, paint);
        if (graphState) {
            startGraphing();
        }

    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        setMeasuredDimension(width, height);
    }
    public void setGraphState(boolean state, int buffer_size) { this.graphState=state;
    this.bufferSize=buffer_size; invalidate();}
    public void startGraphing() {
        byte[] buffer = new byte[bufferSize];
        try {
            jill.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FAILED TO READ BUFFER");
        }
        short[] chunk = Convert.getShortsFromBytes(buffer);
        float[] test = new float[chunk.length * 4];
        iter += pixel_density;
        for (int i = 4; i < chunk.length; i += 4) {
            test[i - 4] = iter;
            test[i - 3] = view_height / 2;
            test[i - 2] = iter;
            test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
        }
        mExtraCanvas.drawLines(test, paint);
        invalidate();
    }

}
/*
TODO know this like the back of your hand ->
 --- THIS CUSTOM VIEWS ---
        $-Can be defined in xml or java code. Different constructors are called based on this context.
            -what are the consequences of each definition?
        $-Why use init method and constructors for each case besides you can declare instances variables with getters and
            setters unlike with a fragment, are each of these constructors called?
            because this way no matter who calls it in any context the same init method is called
        -Why use another canvas then define a bitmap to put inside this canvas?
            then draw on the mExtraCanvas and not the mExtraBitMap?
       -OnMeasure:
            i think this is called if you define the view from java code
        -OnSizeChanged:
            called when size of view has changed -> what changes the size of view?
            i think this is called when the view is defined in xml
        -invalidate:
        -post:
            this causes the runnable action to be added to message queue and run on UI thread
        -postInvalidate:
        -onDraw:
        -runOnUIThread:
        -drawLine vs drawLines:
        -translate:
        -Bitmap:
        -canvas:
        -Path:
        -ValueAnimator:
        -Animator APIs:
        -best practices for decoding DataInputStream

 */