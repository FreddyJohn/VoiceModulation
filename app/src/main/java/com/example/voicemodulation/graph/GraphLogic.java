package com.example.voicemodulation.graph;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import com.example.voicemodulation.audio.AudioCon;
import com.example.voicemodulation.audio.util.Convert;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import com.paramsen.noise.Noise;
public class GraphLogic extends View {
    private static float T1;
    private static float T2;
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private boolean graphState = false;
    private Pair<Canvas,Bitmap> editable;
    private float editable_pos;
    private Pair<Canvas,Bitmap> liveGraph;
    private float graph_pos;
    private Pair<Canvas,Bitmap> liveFFT;
    private float fft_pos;
    private Bitmap SelectBitmap;
    private float iter;
    private RandomAccessFile jacob;
    private Paint x_coordinate_axis;
    private Paint y_coordinate_axis;
    private long audio_length;
    private int[] selection;
    private float density;
    private boolean T1_onScren = false;
    private boolean T2_onScren = false;
    private LinkedList<Bitmap> undo_redo_backStack;
    private boolean reset= false;
    private int[] pixels;
    private float[] fft;
    private float x_resolution;
    private boolean graph_type = true;
    private boolean editable_graph;
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
        density = Convert.numberToDp(context,1);
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
                if(!graphState){
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
                }}
                if(graphState){
                    if(graph_type!=true){
                        graph_type=true;
                        System.out.println("graph_type: "+graph_type);
                    }
                    else if(graph_type){
                        graph_type=false;
                        System.out.println("graph_type: "+graph_type);
                    }
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
                if(!graphState){
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

        }
        return true;
    }
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        this.pixels = new int[(int) ((view_width-pixel_density)*view_height)];
        this.x_resolution = (2048/view_width);
        setMeasuredDimension(width, height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, view_height, view_width, view_height, y_coordinate_axis);
        canvas.drawLine(0, 0, view_width, 0, y_coordinate_axis);
        if(graphState) {
            if(!graph_type){
                doGraphing(canvas,true);}
            if (graph_type) {
                doLiveFFT(canvas);}
       }
       // if (editable_graph) {
        if (!graphState & (liveGraph!=null || liveFFT!=null)) {
            beEditableGraph(canvas);
        }
    }
    private void doLiveFFT(Canvas canvas) {
        if(liveFFT==null){
            liveFFT = makeDrawable((int) view_width, (int)view_height);
        }
        canvas.drawBitmap(liveFFT.second,0,0,paint);
        liveFFT.second.eraseColor(Color.TRANSPARENT);
        byte[] fft_buffer = getAudioChunk(audio_length,2048*2,0);
        float[] chunk = Convert.shortBytesToFloats(fft_buffer);
        Noise noise = Noise.real(2048);
        float[] dst = new float[chunk.length+2];
        fft = noise.fft(chunk, dst);
        float[] test = new float[chunk.length * 4];
        for (int i = 0; i < fft.length/2; i ++) {
            fft_pos+=x_resolution;
            test[i * 4] = fft_pos;
            test[i * 4 + 1] = view_height;
            test[i * 4 + 2] = fft_pos;
            test[i * 4 + 3] = view_height - Math.abs(fft[i*2]) * (view_height / 65535);

        }
        liveFFT.first.drawLines(test, paint);
        fft_pos=0;
        this.audio_length += 2048;
        invalidate();
    }
    private void beEditableGraph(Canvas canvas) {
        System.out.println("parent canvas density: "+canvas.getDensity());
        if(editable==null){
            //makeBitMapp();
            editable = makeDrawable((int) view_width*2, (int)view_height/2);
            generateScaledStaticGraph(editable.first);
        }
        canvas.drawBitmap(editable.second, editable_pos, 0, paint);
        canvas.drawLine(0, view_height/8, view_width, view_height/8, y_coordinate_axis);
        canvas.drawLine(T1, view_height/2, T1, 0, y_coordinate_axis);
        canvas.drawLine(T2, view_height/2, T2, 0, y_coordinate_axis);
    }
    //TODO make me threaded and buffered so that the user can perform modulations while the display is loading
    private void generateScaledStaticGraph(Canvas canvas){
        byte[] buffer = getAudioChunk(0,0,1);
        short[] data = Convert.bytesToShorts(buffer);
        float[] test = new float[data.length * 4];
        float m = (view_width*2)/data.length;
        float norm = ((view_height/2) / 65535);
        float mid_line = view_height/4;
        float pos;
        for (int i = 0; i <data.length ; i++) {
            pos=i*m;
            test[i * 4] = pos;
            test[i * 4 + 1] = mid_line;
            test[i * 4 + 2] = pos;
            test[i * 4 + 3] = mid_line - data[i] * norm;
        }
        canvas.drawLines(test,0, data.length*4,paint);
    }
    private void doGraphing(Canvas canvas,Boolean invalidate)
    {
        if(liveGraph==null){
            liveGraph = makeDrawable((int) view_width, (int)view_height);
        }
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, x_coordinate_axis);
        canvas.drawBitmap(liveGraph.second,0,0,paint);
        if(iter<=view_width){
            startGraphing(liveGraph.first, invalidate);
        }
        if(iter>=view_width-(pixel_density*10))
        {
            graph_pos = (int) (view_width-(pixel_density*10));
            int[] pixels = new int[(int) ((view_width-pixel_density)*view_height)];
            liveGraph.second.getPixels(pixels, 0,(int)(view_width-pixel_density), (int) pixel_density,0,(int)(view_width-pixel_density),(int)view_height);
            liveGraph.second.setPixels(pixels, 0, (int)(view_width-pixel_density), 0,0,(int)(view_width-pixel_density),(int)view_height);
            startGraphing(liveGraph.first,invalidate);
        }
    }
    private  Pair<Canvas,Bitmap> makeDrawable(int view_width,int view_height){
        Bitmap bitmap = Bitmap.createBitmap(view_width,view_height,
                Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(bitmap);
        Pair<Canvas,Bitmap> drawable = new Pair<>(canvas,bitmap);
        return drawable;
    }
    public void setGraphState(boolean state) {
        this.graphState=state;
        editable = null;
        invalidate();
    }
    public void startGraphing(Canvas canvas, Boolean invalidate) {
            byte[] buffer = getAudioChunk(audio_length, (int) audio_length, 1);
            short[] chunk = Convert.bytesToShorts(buffer);
            float[] test = new float[chunk.length * 4];
            iter += pixel_density; // TODO remove iter and replace conditionals with view_width & file length measurement
            graph_pos+=pixel_density;
            for (int i = 0; i < chunk.length; i ++) {
                test[i * 4] = graph_pos;
                test[i * 4 + 1] = view_height / 2;
                test[i * 4 + 2] = graph_pos;
                test[i * 4 + 3] = (view_height / 2) - chunk[i] * (view_height / 65535);
            }
            canvas.drawLines(test, paint);
            invalidate();
    }
    public void moveFileIndex(int progres, int len){
        //TODO do the drawing operations before you call invalidate
       // this.position = progres*(iter/len);
        //this.position= progres * (48000.0f/1000.0f)*2; //back to byte land
        //this.position= progres * 40 * (48000.0f/1000.0f)*2;
        }
    public void test(boolean b) {
        this.graphState=b;
        //if (!b){editable_graph=true;
       // this.graphState=true;}
        }
    private byte[] getAudioChunk(long file_position,int chunk_size, int n){
        try {
            jacob.seek(file_position);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int length=0;
        try {
            length = (int) jacob.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[Math.abs((length*n)-chunk_size)];
        try {
            jacob.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        this.audio_length = length;
        return  buffer;
    }

}
