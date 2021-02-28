package com.example.voicemodulation.graph;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;

import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.util.Convert;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
public class GraphLogic extends View {
    private static float T1;
    private static float T2;
    private float pixel_density;
    //private SeekBar seeker;
    private Paint paint;
    private float view_height;
    private float view_width;
    private int count;
    private boolean graphState = false;
    private Bitmap mExtraBitmapp;
    private Bitmap mExtraBitmap;
    private Bitmap SelectBitmap;
    private Canvas mExtraCanvas;
    private float iter;
    private LinkedList<Short> data;
    private int graph_pos=0;
    private boolean liveAudioState =false;
    private float testing;
    //private int length;
    private float position;
    private float progress;
    private boolean seeking = false;
    private boolean made = false;
    private boolean scaled = false;
    private RandomAccessFile jacob;
    private Paint x_coordinate_axis;
    private Paint y_coordinate_axis;
    private long ballSack;
    private int[] selection;
    private float density;
    private boolean cock = false;
    private boolean T1_onScren = false;
    private boolean T2_onScren = false;
    private LinkedList<Bitmap> undo_redo_backStack;
    private int[] live_display_buffer;
    private Canvas newExtraCanvas;
    private int bitmap_pos;
    private boolean reset= false;

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
        //setFocusable(false);
        undo_redo_backStack = new LinkedList<>();
        density = Convert.numberToDp(context,1);
        data = new LinkedList<>();
        paint = new Paint();
        x_coordinate_axis = new Paint();
        y_coordinate_axis = new Paint();
        x_coordinate_axis.setColor(Color.RED);
        y_coordinate_axis.setColor(Color.rgb(115,115,115));
        x_coordinate_axis.setStyle(Paint.Style.STROKE);
        y_coordinate_axis.setStyle(Paint.Style.STROKE);
        x_coordinate_axis.setStrokeWidth(2-.05f/pixel_density);
        y_coordinate_axis.setStrokeWidth(density);
        paint.setColor(Color.RED);
        //pixel_density = Resources.getSystem().getDisplayMetrics().density;
        pixel_density = Convert.numberToDp(context,1);
        //paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        String name = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
        paint.setStrokeWidth(density);
        AudioCon.IO_RAF funky = new AudioCon.IO_RAF(name);
        //selection =new int[mExtraBitmap.getHeight()*mExtraBitmap.getWidth()];
        jacob = funky.getReadObject();
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        switch (evt.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!T1_onScren) {
                    T1 = evt.getX();
                    System.out.println("T1 on screen");
                    invalidate();
                    T1_onScren=true;
                }
                else if (!T2_onScren && evt.getX()<T1-20) {
                    T2 = evt.getX();
                    System.out.println("T2 on screen");
                    invalidate();
                    T2_onScren=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(evt.getX()>=T2+20 && evt.getX() <= T1-20) {
                    System.out.println("CLICKED INSIDE");
                    //TODO implement undo / redo back stack save and compress but then also where was it
                    try{
                        //live_display_buffer = new int[(int)(view_width*view_height)];
                        //mExtraBitmap.getPixels(live_display_buffer, (int) 0, (int) view_width, (int) 0, 0, (int) view_width, mExtraBitmap.getHeight());
                        selection = new int[(int) (mExtraBitmap.getHeight()*(T1-T2))];
                        SelectBitmap = Bitmap.createBitmap((int)(T1-T2),mExtraBitmap.getHeight(), Bitmap.Config.ALPHA_8);
                        mExtraBitmap.getPixels(selection, (int) 0, (int)(T1-T2), (int) T2, 0, (int) (T1-T2), mExtraBitmap.getHeight());
                        for (int i = 0; i < selection.length; i++) {
                           System.out.println(selection[i]);

                        }
                        SelectBitmap.setPixels(selection, 0, (int)(T1-T2), 0, 0, (int) (T1-T2), mExtraBitmap.getHeight());
                        System.out.println("selection info: "+selection.length+ " "+ Arrays.toString(selection));
                        //TODO store the offsets!!
                        //undo_redo_backStack.put(T1,T2);
                        // you do not need to create more bitmaps
                        // we will use the original memory location since read only
                    } catch (IndexOutOfBoundsException | NullPointerException | IllegalArgumentException e){
                        System.out.println("FAIL"+e); }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (T1-evt.getX()<=20 && evt.getX()>=T2+20) {
                    T1 = evt.getX();
                    System.out.println("T1 on screen and conditional is met " +evt.getX());
                    invalidate();
                }
                //TODO fix these conditional to allow scrolling to Ti+\-n
               if(evt.getX()-T2<=20 && evt.getX()<=T1-20) {
                    T2 = evt.getX();
                    System.out.println("T2 on screen and conditional is met" +evt.getX());
                    invalidate();
                }
        }
        return true;
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        setMeasuredDimension(width, height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        makeBitMap();
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, x_coordinate_axis);
        canvas.drawLine(0, view_height, view_width, view_height, y_coordinate_axis);
        canvas.drawLine(0, 0, view_width, 0, y_coordinate_axis);
        if(graphState) {
            doLiveGraphing(canvas);
        }
        //if(!graphState){
        //    beEditableGraph(canvas);
            //mExtraBitmap.getPixels(live_display_buffer, (int) 0, (int) view_width, (int) T2, 0, (int) view_width, mExtraBitmap.getHeight());
            //live_display_buffer[]

        //}
    }

    private void beEditableGraph(Canvas canvas) {
        //TODO I need to care about display back stack
        // but i do not need to care about how to graph was originally rendered in doLiveGraphing
        // I will recreate the entire graph a display stack at a time being entirely I control of its new coordinate system in respect to file indexing

        //undo_redo_backStack.add(mExtraBitmap);
        if(position<view_width) {
            canvas.drawBitmap(mExtraBitmap,0,0,paint);
        }
        if(position>view_width) {
            canvas.drawBitmap(mExtraBitmap,view_width-position,0,paint);
        }
        canvas.drawLine(position,view_height,position,0, y_coordinate_axis);
       // canvas.drawLine(T1, view_height, T1, 0, y_coordinate_axis);
      // canvas.drawLine(T2, view_height, T2, 0, y_coordinate_axis);

        //try {
            //    java.lang.IllegalArgumentException: abs(stride) must be >= width
            //    java.lang.IllegalArgumentException: y + height must be <= bitmap.height()
            //mExtraBitmap.getPixels(selection, 0, (int) iter, (int) 0, 0, (int) iter, mExtraBitmap.getHeight());
            //mExtraBitmap.getPixels(selection, (int) 0, (int)(T1-T2), (int) T2, 0, (int) (T1-T2), mExtraBitmap.getHeight());
            //mExtraBitmap.setPixel((int)T1,(int)T2,255);
            //for (int i = 0; i <selection.length ; i++) {
            //    selection[i]=selection[i]*0;
            //}
            //SelectBitmap.setPixels(selection, 0, (int)(T1-T2), (int) position, 0, (int) (T1-T2), mExtraBitmap.getHeight());
         //   canvas.drawBitmap(SelectBitmap,position,0,paint);
            //Bitmap bitmap = Bitmap.createBitmap(selection,mExtraBitmap.getHeight(),(int)(T1-T2), Bitmap.Config.ALPHA_8);
            //mExtraBitmap.setPixels(selection, 0, (int)(T1-T2), (int) position, 0, (int) (T1-T2), mExtraBitmap.getHeight());
            //mExtraBitmap.setPixels(selection, 0, (int) iter, (int) iter, 0, (int) iter, mExtraBitmap.getHeight());
      //  } catch (IndexOutOfBoundsException | NullPointerException | IllegalArgumentException e){
     //       System.out.println("FAIL"+e);

      //  }
    }

    private void doLiveGraphing(Canvas canvas) {
        //TODO i do not need to care about display back stack
        // I only need to make sure live input is displayed within view width
        //canvas.drawBitmap(mExtraBitmap,position,0,paint);
        //startGraphing();
        //Canvas canvas1 =null;
        //Bitmap bitmap =null;
        if(iter<=view_width) { // iter is the current position of the waveform in pixels
            position=0; // this is the position that we are drawing the bitmap
            //canvas.drawLine(iter,view_height,iter,0, y_coordinate_axis);
           // canvas1= mExtraCanvas;
           // mExtraCanvas.translate(-.5f,0);
          //  bitmap = mExtraBitmap;
            canvas.drawBitmap(mExtraBitmap,position,0,paint);
            startGraphing(mExtraCanvas);
            //canvas.drawBitmap(mExtraBitmap,position,0,paint);
            //startGraphing(canvas);
        }
        if(iter>=view_width) {
            //scale(canvas, .5f);
            position=view_width-iter;
            if(!reset){
                bitmap_pos=0;
                mExtraBitmapp = Bitmap.createBitmap((int) view_width*2, (int)view_height,
                        Bitmap.Config.ALPHA_8);
                newExtraCanvas = new Canvas(mExtraBitmapp);
                reset = true;

            }
            if(iter>view_width*2)
            {
                mExtraBitmap.recycle();
                canvas.drawBitmap(mExtraBitmapp, position + view_width, 0, paint);
                startGraphing(newExtraCanvas);
                System.out.println("iter: " + iter + " position: " + position + " view_width: " + view_width);
            }
            //mExtraCanvas.translate(-pixel_density,0);
            //System.out.println("POSITION IS: "+position);
            //iter=0;
            //giveMeBitMap();

            //RESETS POS
            //mExtraCanvas.translate(-pixel_density,0);

            //iter=0;
            //mExtraBitmapp = Bitmap.createBitmap((int) view_width, (int)view_height,
           //         Bitmap.Config.ALPHA_8);
          //  newExtraCanvas = new Canvas(mExtraBitmapp);
            //canvas1 =newExtraCanvas;
           // bitmap=mExtraBitmapp;
            //mExtraCanvas.translate(-pixel_density,0);
            //mExtraBitmap.recycle();
            //iter=view_width-pixel_density;
            //position=-pixel_density;
            //iter=0;

            //position=0;
            //canvas.translate(view_width,0);
           // canvas.drawLine(position,view_height,position,0, y_coordinate_axis);
            if(iter<view_width*2) {
                canvas.drawBitmap(mExtraBitmap, position, 0, paint);
                canvas.drawBitmap(mExtraBitmapp, position + view_width, 0, paint);
                startGraphing(newExtraCanvas);
                System.out.println("iter: " + iter + " position: " + position + " view_width: " + view_width);
            }
            //TODO this right here this is the solution to your live display memory problem
            // mExtraBitmap.recycle();
            // even though canvas' have a very large space we must not iterate until we find out how much this is the solution
            //
            //canvas.drawBitmap(mExtraBitmap,position,0,paint);
        }
        //canvas.drawBitmap(mExtraBitmap,position,0,paint);
        //startGraphing(canvas);
    }

    private void makeBitMap() {
        if (!made){
            //(383143x621, max=16384x16384)
            int width = 16384;
            mExtraBitmap = Bitmap.createBitmap(width, (int)view_height,
                    Bitmap.Config.ALPHA_8);
            //mExtraBitmap = Bitmap.createBitmap(width, (int)view_height,
            //        Bitmap.Config.ALPHA_8); //ARGB_8888 //ALPHA_8
            System.out.println("AudioCon.Data.getMemory()="+AudioCon.Data.getMemory());
            //System.out.println("Maximum allowed canvas width="+width);
            System.out.println("Maximum allowed canvas width="+view_width);
            System.out.println("View height is="+ view_height);
            System.out.println("allocated bytes for bitmap="+mExtraBitmap.getAllocationByteCount()/8);
            System.out.println("bitmap diemns="+mExtraBitmap.getHeight()+"x"+mExtraBitmap.getWidth());
            mExtraCanvas = new Canvas(mExtraBitmap);
            made=true;
        }
    }
    private HashMap<Canvas,Bitmap> giveMeBitMap() {
            Bitmap newguy = Bitmap.createBitmap((int) view_width, (int)view_height,
                    Bitmap.Config.ALPHA_8);
            Canvas newExtraCanvas = new Canvas(newguy);
            HashMap display_buffer = new HashMap<>();
            display_buffer.put(newExtraCanvas,newguy);
            return display_buffer;
    }
    public void setGraphState(boolean state) {
        //startGraphing();
        this.graphState=state;
        invalidate();
    }
    public void startGraphing(Canvas canvas) {

            try {
                jacob.seek(ballSack);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int length = 0;
            try {
                length = (int) jacob.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("ballSack=" + ballSack + "  length=" + length);
            byte[] buffer = new byte[(int) (length - ballSack)];
            try {
                count += jacob.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            short[] chunk = Convert.bytesToShorts(buffer);
            float[] test = new float[chunk.length * 4];
            iter += pixel_density;
            bitmap_pos+=pixel_density;
            for (int i = 4; i < chunk.length; i += 4) {
                //test[i - 4] = iter;
               // test[i - 3] = view_height / 2;
               // test[i - 2] = iter;
               // test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
                test[i - 4] = bitmap_pos;
                test[i - 3] = view_height / 2;
                test[i - 2] = bitmap_pos;
                test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
            }
            //mExtraBitmap.setPixels();
            canvas.drawLines(test, paint);
            //refreshDrawableState();
            //canvas.translate(1,0);
            //mExtraCanvas.drawLines(test, paint);
            this.ballSack = length;
            //mExtraCanvas.save();
            invalidate();
    }
    public void moveFileIndex(int progres, int len){
        this.position = progres*(iter/len);
        System.out.println("position="+position);
        System.out.println("iter="+iter);
        System.out.println("progres="+progres);
        System.out.println("len="+len);
        //System.out.println("amount of bytes read="+count);
        invalidate();
    }
    public float getByteCount() {
        return iter;
    }
    public float getDensity() {
        return density;
    }

    public void setT1(int i,int len) {
        T1 = i*(iter/len);
        //T2= len;
        //selection = new int[(int) (mExtraBitmap.getHeight()*iter)];
        //selection = new int[(int) (mExtraBitmap.getHeight()*(T2-T1))];
        //selection = new int[mExtraBitmap.getHeight()*mExtraBitmap.getWidth()];
        //selection = new int[500];
        //invalidate();
    }
    public void setT2(int i, int len) {
        T2 = i*(iter/len);
        //selection = new int[(int) (mExtraBitmap.getHeight()*iter)];
        selection = new int[(int) (mExtraBitmap.getHeight()*(T1-T2))];
        //selection = new int[500];
        invalidate();
    }

    private void scale(Canvas canvas, float v) {
        if(!scaled){
            pixel_density=pixel_density/2;
            canvas.scale(v,v);
            scaled= true;}
    }
    public void test(boolean b) {
        this.graphState=b; }

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