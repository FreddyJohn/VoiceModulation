package com.example.voicemodulation.graph;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import com.example.voicemodulation.audio.util.Convert;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
public class GraphLogic extends View {
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private boolean graphState = false;
    private DataInputStream jill;
    private int bufferSize;
    private LinkedList<Short> data;
    private int graph_pos=0;
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
        paint.setStrokeWidth(pixel_density);
        try {
            String name = Environment.getExternalStorageDirectory().getPath()+"/data.0";
            File i = new File(name);
            this.jill = new DataInputStream(new FileInputStream(name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, paint);
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
    public void setGraphState(boolean state, int buffer_size) { this.graphState=state;
    this.bufferSize=buffer_size; invalidate();}
    public void startGraphing(Canvas canvas) {
        byte[] buffer = new byte[bufferSize];
        try {
            jill.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FAILED TO READ BUFFER");
        }
        short[] chunk = Convert.getShortsFromBytes(buffer);
        for(int i=0; i<chunk.length; i++) {
            graph_pos+=.1;
            //data.add(chunk[i]); //jill has offset + length so theres no need for another data buffer
            canvas.drawLine(graph_pos, view_height / 2, graph_pos, (float) ((view_height / 2) - chunk[i]*.05),paint);
            canvas.translate(+1,0);
            invalidate();
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
        -best practices for decoding DataInputStream:



 */
/*
package com.example.voicemodulation.graph;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.res.ResourcesCompat;
import com.example.voicemodulation.R;
import com.example.voicemodulation.audio.AudioFile;
import com.example.voicemodulation.audio.util.Convert;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedReader;
import java.util.LinkedList;


public class GraphLogic extends View {
    private float pixel_density;
    private int mBackgroundColor;
    private Paint paint;
    private Paint x_coordinate_axis;
    private Paint graph_frame;
    private Path mPath;
    private float view_height;
    private float view_width;
    private Bitmap mExtraBitmap;
    private Canvas mExtraCanvas;
    private InputStream in;
    private Rect mFrame;
    private int graph_pos;

    private boolean graphState = false;
    private AudioFile creation;

    public GraphLogic(Context context) {
        super(context);
        init(context);
    }
    public GraphLogic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public GraphLogic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    public void init(Context context)  //,int graphID)
    {
        paint = new Paint();
        x_coordinate_axis = new Paint();
        graph_frame = new Paint();
        graph_frame.setColor(getResources().getColor(R.color.white));
        x_coordinate_axis.setColor(getResources().getColor(R.color.black));
        x_coordinate_axis.setStrokeWidth(2);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        pixel_density = Resources.getSystem().getDisplayMetrics().density;
        mBackgroundColor = ResourcesCompat.getColor(getResources(),
                R.color.md_grey_700, null);
        mPath = new Path();
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        graph_frame.setStyle(Paint.Style.STROKE);
        graph_frame.setStrokeWidth(20);
        try {
            this.in = new DataInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/data.0"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mPath.moveTo(0, view_height / 2);
    }
    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mExtraBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        mExtraCanvas = new Canvas(mExtraBitmap);
        mExtraCanvas.drawColor(mBackgroundColor);
        int inset = 0;
        mFrame = new Rect(inset, inset, width - inset, height - inset);
        mExtraCanvas.drawLine(0, view_height / 2, view_width, view_height / 2, x_coordinate_axis);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //graphState = true;
        setGraphics(canvas);
        if (graphState) {
            startGraphing();
        }
    }
    public void setGraphState(boolean state) {
        this.graphState = state;
        invalidate();
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        setMeasuredDimension(width, height);
    }

    private void setGraphics(Canvas canvas) {
        canvas.drawBitmap(mExtraBitmap, 0, 0, null);
        canvas.drawLine(graph_pos, 0, graph_pos, view_height, paint);
        canvas.drawRect(mFrame, graph_frame);
    }

    public void startGraphing() {
        //Thread myThread = new Thread(() -> {

            try {
                while(true) {
                    byte[] test = new byte[200]; // length of buffer defined in recordLogic
                    in.read(test);
                    short[] x = Convert.getShortsFromBytes(test);
                    for (int i=0; i>x.length;i++) {
                        //line[]
                        System.out.println(i);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            while(true) {
                try {
                        System.out.println(in.read());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            try {
                while (true) {
                    short i = (short) silentBob.read();

                    //int i = silentBob.read();
                    //System.out.println(i);

                    i= (short) (i*.010);
                    //i= (short) (i*(view_height/i));
                    if (i != -1 ) {
                        //graph_pos += pixel_density;
                        graph_pos += 1;
                        //System.out.println(i);
                        mPath.quadTo(graph_pos, view_height / 2, graph_pos, (view_height / 2) - i);
                        mPath.quadTo(graph_pos, (view_height / 2) + i, graph_pos, (view_height / 2) - i);
                        mExtraCanvas.drawPath(mPath, paint);
                        //mExtraCanvas.skew(010, 1092648968);

                        postInvalidate();
                    }




                }
            } catch (IOException e) {
                System.out.println(" PipeThread Exception: " + e);
            }




        //});
        //myThread.start();
    }

}
*/