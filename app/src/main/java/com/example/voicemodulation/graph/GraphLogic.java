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

import com.example.voicemodulation.audio.AudioCon.Data;
import com.example.voicemodulation.audio.AudioCon.IO_RAF;
import com.example.voicemodulation.audio.util.Convert;
import com.example.voicemodulation.audio.PieceTable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import com.paramsen.noise.Noise;
/*
TODO
    first what should happen is graphing.
        a.) a live FFT plot
        b.) b live graph display
    second thing that should happen is editing.
        editing should be instant.
        the O(n) nightmare, it creeps in every corner of this project.
            how on earth can we make editing instant?
            how on earth could we avoid the file taking longer to load as it gets longer?
                on Summary information,



 */
public class GraphLogic extends View {
    private static float T1;
    private static float T2;
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private boolean graphState = false;
    private Pair<Canvas,Bitmap> editable;
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
    private float[] fft;
    private float select_pos_x;
    private float select_pos_y;
    private float x_resolution;
    private boolean graph_type = true;
    private PieceTable pieceTable;

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
        IO_RAF funky = new IO_RAF(name);
        jacob = funky.getWriteObject(false);
    }
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        switch (evt.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!graphState && evt.getY()<=view_height/2){
                    SelectBitmap=null;
                    if (!T1_onScren) {
                        T1 = evt.getX();
                        invalidate();
                        T1_onScren=true;
                    }
                    else if (!T2_onScren && evt.getX()<T1-20) {
                        T2 = evt.getX();
                        invalidate();
                        T2_onScren=true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!graphState){
                if(evt.getX()>=T2+20 && evt.getX() <= T1-20 && evt.getY()<=view_height/2) {
                    selection = new int[(int) (editable.second.getHeight()*(T1-T2))];
                    editable.second.getPixels(selection, 0, (int)(T1-T2), (int) T2, 0, (int) (T1-T2), editable.second.getHeight());
                    SelectBitmap = Bitmap.createBitmap(selection,(int)(T1-T2),editable.second.getHeight(), Bitmap.Config.ALPHA_8);
                    select_pos_x=T2;
                    select_pos_y=view_height/2;
                    invalidate();
                    //TODO implement undo / redo back stack save and compress but then also where was it
                }
                if(SelectBitmap!=null && evt.getY()<=view_height/2 && !T1_onScren && !T2_onScren){
                    Pair pair = getSelectionPoints();
                    try {
                        jacob.seek(pieceTable._text_len);
                        jacob.write(pieceTable.find((int)pair.first,(int)pair.second-(int)pair.first));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pieceTable.add((int)pair.second-(int)pair.first,(int)pair.first);
                    editable=null;
                    SelectBitmap = null;
                    invalidate();
                    }
                }
                else if(graphState){
                    if(graph_type!=true){
                        graph_type=true;
                    }
                    else if(graph_type){
                        graph_type=false;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!graphState && evt.getY()<=view_height/2){
                    if (T1-evt.getX()<=20 && evt.getX()>=T2+20) {
                        T1 = evt.getX();
                        invalidate();
                    }
                    if(evt.getX()-T2<=20 && evt.getX()<=T1-20) {
                        T2 = evt.getX();
                        invalidate();
                    }
                }
                else if( evt.getY()>=view_height/2 && SelectBitmap != null ){
                    select_pos_x=evt.getX();
                    select_pos_y=evt.getY()-(SelectBitmap.getHeight()/2);
                    T1_onScren=false;
                    T2_onScren = false;
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
        if (!graphState & (liveGraph!=null || liveFFT!=null)) {
            beEditableGraph(canvas);
            canvas.drawLine(0,view_height/2,view_width,view_height/2,y_coordinate_axis);
            if(SelectBitmap!=null){
                canvas.drawBitmap(SelectBitmap,select_pos_x,select_pos_y,paint);
            }
        }
    }
    private void doLiveFFT(Canvas canvas) {
        if(liveFFT==null){
            liveFFT = makeDrawable((int) view_width, (int)view_height,0);
            doLiveFFT(canvas);
        }
        canvas.drawBitmap(liveFFT.second,0,0,paint);
        liveFFT.second.eraseColor(Color.TRANSPARENT);
        byte[] fft_buffer = Data.getAudioChunk(audio_length,2048*2,0,jacob);
        float[] chunk = Convert.shortBytesToFloats(fft_buffer);
        Noise noise = Noise.real(2048);
        float[] dst = new float[chunk.length+2];
        fft = noise.fft(chunk, dst);
        float[] test = new float[chunk.length * 4];
        float norm = (view_height / 65535);
        for (int i = 0; i < fft.length/2; i ++) {
            fft_pos+=x_resolution;
            test[i * 4] = fft_pos;
            test[i * 4 + 1] = view_height;
            test[i * 4 + 2] = fft_pos;
            test[i * 4 + 3] = view_height - Math.abs(fft[i*2]) * norm;

        }
        liveFFT.first.drawLines(test, paint);
        fft_pos=0;
        audio_length += 2048;
        invalidate();
    }
    private void beEditableGraph(Canvas canvas) {
        if(editable==null){
            editable = makeDrawable((int) view_width, (int)view_height, (int) audio_length);
            generateScaledStaticGraph(editable.first);
        }
        canvas.drawBitmap(editable.second, 0, 0, paint);
        if (T2_onScren || T1_onScren){
        canvas.drawLine(T1, view_height/2, T1, 0, y_coordinate_axis);
        canvas.drawLine(T2, view_height/2, T2, 0, y_coordinate_axis);}
    }
    //TODO make me threaded and buffered so that the user can perform modulations while the display is loading
    private void generateScaledStaticGraph(Canvas canvas){
        byte[] buffer = pieceTable.get_text();
        short[] data = Convert.bytesToShorts(buffer);
        float[] test = new float[data.length * 4];
        //float m = view_width/data.length;
        float m = view_width/(data.length/256);
        float norm = ((view_height/2) / 65535);
        float mid_line = view_height/4;
        float pos;
        for (int i = 0; i <data.length/256; i++) {
        //for (int i = 0; i <data.length; i++) {
            short min = data[i];
            short max = data[i];
            for (int j = 0; j < 256; j++) {
                if (max<data[i*256]){
                        max=data[i*256];
                }
                if (min<data[i*256]){
                    min=data[i*256];
                }

            }
            pos= i*m;
            test[i * 4] = pos;
            test[i * 4 + 1] = mid_line + min * norm;
            test[i * 4 + 2] = pos;
            test[i * 4 + 3] = mid_line - max * norm;

            /*
            pos= i*m;
            test[i * 4] = pos;
            test[i * 4 + 1] = mid_line;
            test[i * 4 + 2] = pos;
            test[i * 4 + 3] = mid_line - data[i] * norm;

             */
             }
        canvas.drawLines(test,paint);
    }

    private void doGraphing(Canvas canvas,Boolean invalidate)
    {
        if(liveGraph==null){
            liveGraph = makeDrawable((int) view_width, (int)view_height,0);
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
    private  Pair<Canvas,Bitmap> makeDrawable(int view_width,int view_height,int density){
        Bitmap bitmap = Bitmap.createBitmap(view_width,view_height,
                Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setDensity(density);
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
    public Pair<Integer,Integer> getSelectionPoints(){
        //float norm = view_width/audio_length;
        //jacob.length()
        float norm = 0;
        try {
            norm = jacob.length()/2/view_width;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //int first = (int) (T2/norm);
        int first = (int) (T2*norm);
        if (first%2==1){first-=1;}
        //int second = (int) (T1/norm);
        int second = (int) (T1*norm);
        if (second%2==1){second-=1;}
        Pair pair=new Pair<>(first,second);
        System.out.println("ladies and gentlemen the pairs: "+pair.first+","+pair.second);
        return pair;
    }

    public void setTable(PieceTable pieceTable) {
        this.pieceTable = pieceTable;
    }

    public void setT2(int progress, int i) {
    }
}
