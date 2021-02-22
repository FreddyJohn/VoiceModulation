package com.example.voicemodulation.graph;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import com.example.voicemodulation.audio.util.Convert;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
public class GraphLogic extends View {
    private float pixel_density;
    private SeekBar seeker;
    private Paint paint;
    private float view_height;
    private float view_width;
    private int trueWidth;
    private int count;
    private boolean graphState = false;
    private DataInputStream jill;
    private DataInputStream jane;
    private int bufferSize;
    private int _error;
    private Bitmap mExtraBitmap;
    private Canvas mExtraCanvas;
    private int iter;
    private LinkedList<Short> data;
    private int graph_pos=0;
    private boolean liveAudioState =false;
    private float testing;
    private int length;
    private float position;
    private int progress;
    /*
    TODO SIMPLIFICATION
        "because they are not the default canvas and bitmap used in the onDraw() method."
        -https://developer.android.com/codelabs/advanced-android-training-draw-on-canvas#2
            so use the default canvas and bitmap in the onDraw

     */
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
        //pixel_density = Resources.getSystem().getDisplayMetrics().density;
        pixel_density = Convert.numberToDp(context,1);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7-.05f/pixel_density);
        try {
            //TODO fix this shit
            String name = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
            File i = new File(name);
            this.jill = new DataInputStream(new FileInputStream(name));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    /*
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        int action = evt.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                System.out.println( "UP");
                break;
            case MotionEvent.ACTION_UP:
                System.out.println( "UP");
                break;
            case MotionEvent.ACTION_MOVE:
                count+=1; //the iter is a variable given by the length of the file
                seeker.setProgress(count);
                System.out.println(evt.getX());
                break;
        }

        return true;
    }

     */
    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (oldWidth==0 && oldHeight==0)
        {
            mExtraBitmap = Bitmap.createBitmap(width+10000, height,
                    Bitmap.Config.ARGB_8888);
            //mExtraBitmap.setDensity(20);
            mExtraCanvas = new Canvas(mExtraBitmap);
            view_width = width;
            trueWidth = width;
            view_height = height;
        } else {
            view_width = oldWidth;
            view_height = oldHeight;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(iter>view_width) {
            position=view_width-iter;
        }
        if(iter<view_width) {
            position=0;
        }
        if(!graphState){
            canvas.drawBitmap(mExtraBitmap,progress,0,null);

            //canvas.drawLine(progress,view_height,progress,0,paint);
            //position=0;
        }
        if (graphState) {
            canvas.drawBitmap(mExtraBitmap,position,0,null);
            startGraphing(canvas);
        }
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, paint);
        canvas.drawBitmap(mExtraBitmap,position,0,null);
        startGraphing(canvas);
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        setMeasuredDimension(width, height);
    }
    public void setGraphState(boolean state, int buffer_size) {
        this.graphState=state;
        this.bufferSize=buffer_size; invalidate();
    }
    public void startGraphing(Canvas canvas) {
        byte[] buffer = new byte[bufferSize];
        try {
            jill.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        short[] chunk = Convert.bytesToShorts(buffer);
        float[] test = new float[chunk.length * 4];
        iter += pixel_density;
        count+=1;
        for (int i = 4; i < chunk.length; i += 4) {
            test[i - 4] = iter;
            test[i - 3] = view_height / 2;
            test[i - 2] = iter;
            test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
        }
        mExtraCanvas.drawLines(test, paint);
        //canvas.drawLines(test, paint);
        invalidate();
    }

    public void setSeekBar(SeekBar the_seeker) {
        this.seeker = the_seeker;
        this.length = seeker.getMax();
    }
    public void moveFileIndex(int progress, int sampleRate){
        //TODO read docs on read -> "The number of bytes actually read is returned as an integer"
        // https://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html#read%28byte%5B%5D%29
        int sampleToPixel = progress*(sampleRate/1000);
        if (sampleToPixel!=0){
            System.out.println(" iter/sampleToPixel="+ sampleToPixel/iter);
            this.progress = (int)sampleToPixel/iter;
           // invalidate();
            }

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