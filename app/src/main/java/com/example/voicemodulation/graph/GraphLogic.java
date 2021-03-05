package com.example.voicemodulation.graph;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.util.Convert;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
public class GraphLogic extends View {
    private static float T1;
    private static float T2;
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private int count;
    private boolean graphState;
    private Bitmap mExtraBitmapp;
    private Bitmap mExtraBitmap;
    private Bitmap SelectBitmap;
    private Canvas mExtraCanvas;
    private float iter;
    private LinkedList<Short> data;
    private int graph_pos=0;
    private boolean liveAudioState =false;
    private float position=0;
    private boolean seeking = false;
    private boolean made = false;
    private boolean scaled = false;
    private RandomAccessFile jacob;
    private Paint x_coordinate_axis;
    private Paint y_coordinate_axis;
    private long ballSack;
    private int[] selection;
    private float density;
    private boolean T1_onScren = false;
    private boolean T2_onScren = false;
    private LinkedList<Bitmap> undo_redo_backStack;
    private Canvas newExtraCanvas;
    private LinkedList<Integer> pos;
    private int bitmap_pos;
    private boolean reset= false;

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
    public void init(Context context, AttributeSet attributeSet)
    {
        undo_redo_backStack = new LinkedList<>();
        pos = new LinkedList<Integer>();
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
        pixel_density = Convert.numberToDp(context,1);
        paint.setStyle(Paint.Style.STROKE);
        String name = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
        paint.setStrokeWidth(density);
        AudioCon.IO_RAF funky = new AudioCon.IO_RAF(name);
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
                /*
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

                 */
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
            //reset=false;
            doGraphing(canvas);
        }
        if(!graphState & pos.size()==2){
            beEditableGraph(canvas);
       }
    }
    public void startGraphingg(Canvas canvas) {
        if(pos.size()==2) {
            if (pos.get(0) - pos.get(1) != 0 && pos.get(0)-pos.get(1)>0) {
                int[] pixels = new int[(int) ((view_width-pixel_density)*view_height)];
                try {
                    jacob.seek(pos.get(0));
                } catch (IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[pos.get(0) - pos.get(1)];
                try {
                    count += jacob.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                short[] chunk = Convert.bytesToShorts(buffer);
                float[] test = new float[chunk.length * 4];
                bitmap_pos += pixel_density;
                for (int i = 4; i < chunk.length; i += 4) {
                     test[i - 4] = bitmap_pos;
                     test[i - 3] = view_height / 2;
                     test[i - 2] = bitmap_pos;
                     test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
                     //for (int j = 0; j < chunk[i]; j ++) {
                    //    mExtraBitmap.setPixel(bitmap_pos, (int) ((view_height / 2) - i * (view_height / 65535)),75);
                    }
                canvas.drawLines(test, paint);
                mExtraBitmapp.getPixels(pixels, 0,(int)(view_width-pixel_density), (int)pixel_density,0,(int)(view_width-pixel_density),(int)view_height);
                mExtraBitmapp.setPixels(pixels, 0, (int)(view_width-pixel_density), 0,0,(int)(view_width-pixel_density),(int)view_height);
            }
        }
    }
    private void beEditableGraph(Canvas canvas) {
        //TODO
        // but i do not need to care about how to graph was originally rendered in doLiveGraphing
        // I will recreate the entire graph a display stack at a time being entirely I control of its new coordinate system in respect to file indexing
        makeBitMapp();
       // canvas.drawLine(position,view_height,position,0, y_coordinate_axis);
        canvas.drawBitmap(mExtraBitmapp, 0, 0, paint);
        canvas.drawLine(T1, view_height, T1, 0, y_coordinate_axis);
        canvas.drawLine(T2, view_height, T2, 0, y_coordinate_axis);
        if(T1_onScren==false & T2_onScren==false) {
            bitmap_pos = (int) (view_width-(pixel_density*50));
            startGraphingg(newExtraCanvas);
        }
    }
    private void doGraphing(Canvas canvas)
    {
       canvas.drawBitmap(mExtraBitmap,0,0,paint);
        if(iter<=view_width){
            startGraphing(mExtraCanvas);
        }
        if(iter>=view_width-(pixel_density*10))
        {
            bitmap_pos = (int) (view_width-(pixel_density*10));
           // bitmap_pos = (int) (view_width-(pixel_density*50));
            int[] pixels = new int[(int) ((view_width-pixel_density)*view_height)];
            mExtraBitmap.getPixels(pixels, 0,(int)(view_width-pixel_density), (int) pixel_density,0,(int)(view_width-pixel_density),(int)view_height);
            mExtraBitmap.setPixels(pixels, 0, (int)(view_width-pixel_density), 0,0,(int)(view_width-pixel_density),(int)view_height);
           // mExtraBitmap.getPixels(pixels, 0,(int)(view_width-pixel_density), 0,0,(int)(view_width-pixel_density),(int)view_height);
           // mExtraBitmap.setPixels(pixels, 0, (int)(view_width-pixel_density), (int)pixel_density,0,(int)(view_width-pixel_density),(int)view_height);
            startGraphing(mExtraCanvas);
        }
    }

    private void makeBitMap() {
        if (!made){
            //(383143x621, max=16384x16384)
            //int width = 16384;
            mExtraBitmap = Bitmap.createBitmap((int) view_width, (int)view_height,
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

    private void makeBitMapp() {
        if (!reset){
            //(383143x621, max=16384x16384)
            //int width = 16384;
            mExtraBitmapp = Bitmap.createBitmap((int) view_width, (int)view_height,
                    Bitmap.Config.ALPHA_8);
            newExtraCanvas = new Canvas(mExtraBitmapp);
            reset=true;
        }
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
                test[i - 4] = bitmap_pos;
                test[i - 3] = view_height / 2;
                test[i - 2] = bitmap_pos;
                test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
            }
            canvas.drawLines(test, paint);
            this.ballSack = length;
            invalidate();
    }
    public void moveFileIndex(int progres, int len){
        //TODO do the drawing operations before you call invalidate
       // this.position = progres*(iter/len);
        this.position= progres * (48000.0f/1000.0f)*2; //back to byte land
        //this.position= progres * 40 * (48000.0f/1000.0f)*2;
        pos.add((int)position);
        if(pos.size()>2){
            pos.removeFirst();
        }
        System.out.println("position="+position);
        System.out.println("iter="+iter);
        System.out.println("progres="+progres);
        System.out.println("len="+len);
        System.out.println("pos track: "+pos);
        invalidate();
        /*
        makeBitMapp();
        // canvas.drawLine(position,view_height,position,0, y_coordinate_axis);
        mExtraCanvas.drawBitmap(mExtraBitmapp, 0, 0, paint);
        mExtraCanvas.drawLine(T1, view_height, T1, 0, y_coordinate_axis);
        mExtraCanvas.drawLine(T2, view_height, T2, 0, y_coordinate_axis);
        if(T1_onScren==false & T2_onScren==false) {
            bitmap_pos = (int) (view_width-(pixel_density*50));
        if(pos.size()==2) {
            if (pos.get(0) - pos.get(1) != 0 && pos.get(0)-pos.get(1)>0) {
                int[] pixels = new int[(int) ((view_width-pixel_density)*view_height)];
                try {
                    jacob.seek(pos.get(0));
                } catch (IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[pos.get(0) - pos.get(1)];
                try {
                    count += jacob.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                short[] chunk = Convert.bytesToShorts(buffer);
                float[] test = new float[chunk.length * 4];
                bitmap_pos += pixel_density;
                 for (int i = 4; i < chunk.length; i += 4) {
                    test[i - 4] = bitmap_pos;
                    test[i - 3] = view_height / 2;
                    test[i - 2] = bitmap_pos;
                    test[i - 1] = (view_height / 2) - chunk[i] * (view_height / 65535);
                    //for (int j = 0; j < chunk[i]; j ++) {
                    //    mExtraBitmap.setPixel(bitmap_pos, (int) ((view_height / 2) - i * (view_height / 65535)),75);
                }
                newExtraCanvas.drawLines(test, paint);
                mExtraBitmapp.getPixels(pixels, 0,(int)(view_width-pixel_density), (int)pixel_density,0,(int)(view_width-pixel_density),(int)view_height);
                mExtraBitmapp.setPixels(pixels, 0, (int)(view_width-pixel_density), 0,0,(int)(view_width-pixel_density),(int)view_height);
                invalidate();
            }

         */
        }

    public float getByteCount() {
        return iter;
    }
    public float getDensity() {
        return density;
    }

    public void setT1(int i,int len) {
        T1 = i*(iter/len);

    }
    public void setT2(int i, int len) {
        T2 = i*(iter/len);
        selection = new int[(int) (mExtraBitmap.getHeight()*(T1-T2))];
        invalidate();
    }

    public void test(boolean b) {
        this.graphState=b; }
}
