package com.example.voicemodulation.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.example.voicemodulation.R;

import java.io.IOException;
import java.io.PipedReader;
import java.util.LinkedList;


public class GraphLogic extends View {
    private final float pixel_density;
    private final int mBackgroundColor;
    private final Paint paint;
    private final Paint x_coordinate_axis;
    private final Paint graph_frame;
    private Path mPath;
    private float view_height;
    private PipedReader silentBob;
    private float view_width;
    private Bitmap mExtraBitmap;
    private Canvas mExtraCanvas;
    private Rect mFrame;
    private int graph_pos;
    private LinkedList<Float> display_buffer;
    private boolean graphState = false;


    public GraphLogic(Context context) {
        this(context, null);
    }

    public GraphLogic(Context context, AttributeSet attributeSet) //,int graphID)
    {
        super(context);
        display_buffer = new LinkedList<>();
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
        paint.setStrokeWidth(pixel_density);
        graph_frame.setStyle(Paint.Style.STROKE);
        graph_frame.setStrokeWidth(20);
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
        setGraphics(canvas);
        if (graphState) {
            //setGraphics(canvas);
            startGraphing();
        }
        //draw ticks
        //draw frame
        //draw title
        //push back graph i don't care about scrolling on waveform

    }

    public void setGraphState(boolean state, PipedReader silentBob) {
        this.graphState = state;
        this.silentBob = silentBob;
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
        //canvas.drawLine(0,view_height/2,view_width,view_height/2,x_coordinate_axis);
        //canvas.drawRect(mFrame,graph_frame);
    }

    //meet bob event handling code
    public void startGraphing() {
        Thread myThread = new Thread(() -> {
            try {
                while (true) {
                    short i = (short) silentBob.read();
                    if (i != -1 && i >= 0) {
                        graph_pos += pixel_density;
                        graph_pos += 1;
                        //System.out.println(i);
                        mPath.quadTo(graph_pos, view_height / 2, graph_pos, (view_height / 2) - i);
                        mPath.quadTo(graph_pos, (view_height / 2) + i, graph_pos, (view_height / 2) - i);
                        mExtraCanvas.drawPath(mPath, paint);
                        postInvalidate();
                    }
                }
            } catch (IOException e) {
                System.out.println(" PipeThread Exception: " + e);
            }
        });
        myThread.start();
    }

}
