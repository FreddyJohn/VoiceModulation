package com.example.voicemodulation.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.util.Convert;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;


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
    private double iter=.1;
    private int dynamicRange;

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

    public void init(Context context, AttributeSet attributeSet) //,int graphID)
    {
        LinearLayout.LayoutParams view_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        view_params.gravity = Gravity.CENTER;
        data = new LinkedList<>();
        paint = new Paint();
        paint.setColor(Color.RED);
        pixel_density = Convert.numberToDp(context,1);
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
        if (graphState & dynamicRange==Short.MAX_VALUE*2+1) {
            startGraphing(canvas);
        }
        if(graphState & dynamicRange==Byte.MAX_VALUE*2+1){
            startGraphingBytes(canvas);
        }
    }
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        setMeasuredDimension(width, height);
    }
    private void skipBytes(int offset){
        try {
            jane.skipBytes(offset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setEncoding(int _dynamicRange){this.dynamicRange=_dynamicRange;}
    public void setGraphState(boolean state, int buffer_size,String in_file,int n) {
        this.graphState=state;
        this.bufferSize=buffer_size;
        System.out.println("Buffer size: "+buffer_size);
        try {
            this.jane = new DataInputStream(new FileInputStream(in_file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        switch (n){
            case 0:
                skipBytes(0);
                this.bufferSize= (int) view_width*2;
                break;
            case 1:
                AudioCon.IO_RAF con = new AudioCon.IO_RAF(in_file);
                RandomAccessFile f = con.getReadObject();
                try {
                    skipBytes((int) f.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        invalidate(); }
    public void startGraphing(Canvas canvas) {
        byte[] buffer = new byte[bufferSize];
        short[] chunk = new short[bufferSize*2];
        try {
            jane.read(buffer);
            chunk = Convert.bytesToShorts(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("FAILED TO READ BUFFER");
        }
        for(int i=0; i<chunk.length; i++) {
            graph_pos += iter;
            //canvas.drawLine(graph_pos, view_height / 2, graph_pos, (view_height / 2) - chunk[i] * (view_height / dynamicRange), paint);
            canvas.drawPoint(graph_pos, (view_height / 2) - chunk[i] * (view_height / dynamicRange), paint);
            canvas.translate(+1, 0);
        }
        invalidate();
    }
    public void startGraphingBytes(Canvas canvas) {
        byte[] buffer = new byte[bufferSize];
        try {
            jane.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("FAILED TO READ BUFFER");
        }
        for(int i=0; i<buffer.length; i++) {
            graph_pos += iter;
            canvas.drawLine(graph_pos, view_height / 2, graph_pos, (view_height / 2) - buffer[i], paint);
            canvas.translate(+1, 0);
        }
        invalidate();
    }
}
