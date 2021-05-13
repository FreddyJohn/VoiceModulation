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
import com.example.voicemodulation.audio.AudioCon.IO_RAF;
import com.example.voicemodulation.sequence.PieceTable;
import com.example.voicemodulation.sequence.PieceTable._Piece;
import com.example.voicemodulation.util.Convert;
import com.example.voicemodulation.sequence.BitmapPieceTable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;

public class GraphLogic extends View {
    private static float T1;
    private static float T2;
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private boolean graphState = false;
    private Bitmap editable;
    private Drawable liveGraph;
    private Drawable liveFFT;
    private float graph_pos;
    private float fft_pos;
    private Bitmap SelectBitmap;
    private float iter;
    private float theta, omega;
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
    private boolean graph_type = false;
    private PieceTable pieceTable;
    private BitmapPieceTable bitmapPieceTable;
    private Drawable liverGraph;
    private int x_0;
    private RandomAccessFile jennifer;

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
        setHorizontalScrollBarEnabled(true);
        String namer= Environment.getExternalStorageDirectory().getPath()+"/coochie";
        IO_RAF groovy = new IO_RAF(namer);
        jennifer = groovy.getWriteObject(false);
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
    /*
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        super.setOnScrollChangeListener(l);
    }

     */


            @Override
            public boolean onTouchEvent(MotionEvent evt) {
                switch (evt.getAction()) {
                    /*
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
                            selection = new int[(int) (editable.bitmap.getHeight()*(T1-T2))];
                            //editable.bitmap.getPixels(selection, 0, (int)(T1-T2), (int) T2, 0, (int) (T1-T2), editable.bitmap.getHeight());
                            //SelectBitmap = Bitmap.createBitmap(selection,(int)(T1-T2),editable.bitmap.getHeight(), Bitmap.Config.ALPHA_8);
                            editable.bitmap.getPixels(selection, 0, (int)(T1-T2), (int) T2, 0, (int) (T1-T2), editable.bitmap.getHeight());
                            SelectBitmap = Bitmap.createBitmap(selection,(int)(T1-T2),editable.bitmap.getHeight(), Bitmap.Config.ALPHA_8);
                            //int[] dimens_test = new int[selection.length];
                            //TODO what is ALPHA_8 and does it store and represent bitmap color information within int[] ?
                            //  _ _ _ _ == _
                            //             _
                            //             _
                            //             _
                            //  in other words, it stores a summation of unordered line segments that directly correspond to the rows AND NOT the columns of the bitmap
                            //  So in order to move from rows->columns we must process over the entire bitmap whatever its original length was which is total bummer!
                            //  the length of a row in this case is T1-T2
                            //  we could iterate through the summation of rows and append only the data from
                            //  row_i < x < (T1-T2) / horizontal split
                            //  what is a column than just a summation of row with width 1
                            //  conversely, what is a row than just a summation of columns with height 1

                            // for(int i=0; i<selection.length/2;i++){
                            //         dimens_test[i] = selection[i];
                            //     }
                            System.out.println("is evenly divisible ? " + selection.length/(int)(T1-T2));
                           // SelectBitmap = Bitmap.createBitmap(dimens_test,(int)(T1-T2),editable.bitmap.getHeight(), Bitmap.Config.ALPHA_8);
                            select_pos_x=T2;
                            select_pos_y=view_height/2;
                            invalidate();
                            //TODO implement undo / redo back stack save and compress but then also where was it
                        }
                        if(SelectBitmap!=null && evt.getY()<=view_height/2 && !T1_onScren && !T2_onScren){
                            Pair pair = getSelectionPoints();
                            int action_up = (int) evt.getX()/4;
                            try {
                                jacob.seek(pieceTable._text_len);
                                jennifer.seek(bitmapPieceTable._text_len);
                                jacob.write(pieceTable.find((int)pair.first,(int)pair.second-(int)pair.first));
                                jennifer.write(Convert.IntsToBytes(selection));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bitmapPieceTable.add(SelectBitmap.getAllocationByteCount(), (action_up));
                            System.out.println("action up "+action_up);
                            System.out.println("byte count "+SelectBitmap.getAllocationByteCount());
                            System.out.println("selection length "+ selection.length);
                            System.out.println("width "+SelectBitmap.getWidth()+" , "+"height "+SelectBitmap.getHeight());
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
                        */
                    case MotionEvent.ACTION_MOVE:
                        /*
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
                            select_pos_y=evt.getY()-(SelectBitmap.getHeight());
                            T1_onScren=false;
                            T2_onScren = false;
                            invalidate();
                        }

                         */
                      //  else if (evt.getY()>=view_height/2){
                            scrollTo((int) evt.getX(), (int)(evt.getY()+(view_height/2)));
                      //  }
                }
                return true;
            }
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        this.view_width = MeasureSpec.getSize(width);
        this.view_height = MeasureSpec.getSize(height);
        this.x_resolution = (2048/view_width);
        this.bitmapPieceTable.set_dimens((int) view_height/2, (int)view_width, (int) pixel_density);
        setMeasuredDimension(width, height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.rotate(-90);
        scrollTo((int) 0, (int) -view_height/2);
        //canvas.translate(0,view_height/2);
      //  canvas.drawLine(0, view_height, view_width, view_height, y_coordinate_axis);
      //  canvas.drawLine(0, 0, view_width, 0, y_coordinate_axis);
        if(graphState) {
            //if(!graph_type){
            doGraphing(canvas,true);
            //}
           // if (graph_type) {
           //     doLiveFFT(canvas);}
       }
        if (!graphState  & liveGraph!=null ){ //|| liveFFT!=null)) {
            beEditableGraph(canvas);
           // canvas.drawLine(0,view_height/2,view_width,view_height/2,y_coordinate_axis);
            if(SelectBitmap!=null){
                canvas.drawBitmap(SelectBitmap,select_pos_x,select_pos_y,paint);
            }
        }
    }
    /*
    private void doLiveFFT(Canvas canvas) {
        if(liveFFT==null){
            liveFFT = new Drawable((int) view_width, (int)view_height,-90);
            doLiveFFT(canvas);
        }
        canvas.drawBitmap(liveFFT.bitmap,0,0,paint);
        liveFFT.bitmap.eraseColor(Color.TRANSPARENT);
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
        liveFFT.canvas.drawLines(test, paint);
        fft_pos=0;
        audio_length += 2048;
        invalidate();
    }
     */
    private void doGraphing(Canvas canvas,Boolean invalidate){
        if(graph_pos<=view_width) {
            if(liveGraph==null){
                liveGraph = new Drawable((int) view_height/2, (int)view_width);
            }
            canvas.drawBitmap(liveGraph.bitmap,0,0,paint);
            startGraphing(liveGraph.canvas);
        }
        if(graph_pos>=view_width-pixel_density*10) {
            graph_pos = (int) (view_width-pixel_density*10);
            int[] pixels = new int[liveGraph.bitmap.getAllocationByteCount()];
            liveGraph.bitmap.getPixels(pixels,0,liveGraph.bitmap.getWidth(),0,0,liveGraph.bitmap.getWidth(),liveGraph.bitmap.getHeight());
            int[] test = Arrays.copyOfRange(pixels,liveGraph.bitmap.getWidth(), pixels.length); // view width is actually the height after -90 degree translation
            liveGraph.bitmap.setPixels(test,0, (int) (liveGraph.bitmap.getWidth()),0,0, (int) (liveGraph.bitmap.getWidth()), (int) (liveGraph.bitmap.getHeight()-pixel_density));
            startGraphing(liveGraph.canvas);
        }
        int[] pixels = new int[(int) (pixel_density*liveGraph.bitmap.getWidth())]; // view width is actually the height after -90 degree translation
        liveGraph.bitmap.getPixels(pixels, 0,(int)liveGraph.bitmap.getWidth(),  0,(int) (graph_pos-pixel_density),(int)liveGraph.bitmap.getWidth(), (int) pixel_density);
        try {
            jennifer.seek(jennifer.length());
            jennifer.write(Convert.IntsToBytes(pixels));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGraphState(boolean state) {
        this.graphState=state;
        editable = null;
        invalidate();
    }

    public void startGraphing(Canvas canvas) {
            byte[] buffer = getAudioChunk(audio_length, (int) audio_length, 1);
            short[] chunk = Convert.bytesToShorts(buffer);
            float[] test = new float[chunk.length * 4];
            graph_pos+=pixel_density;
            for (int i = 0; i < chunk.length; i ++) {
                test[i * 4] = view_height / 4;
                test[i * 4 + 1] = graph_pos;
                test[i * 4 + 2] =  (view_height / 4) - chunk[i] * ((view_height/2) / 65535);
                test[i * 4 + 3] = graph_pos;
                /*
                test[i * 4] = graph_pos;
                test[i * 4 + 1] = view_height / 4;
                test[i * 4 + 2] = graph_pos;
                test[i * 4 + 3] = (view_height / 4) - chunk[i] * (view_height / 65535);

                 */
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
        this.audio_length = length; //TODO make two sep var for fft and graph
        return  buffer;
    }
    public Pair<Integer,Integer> getSelectionPoints(){

        float norm = 0;
        try {
            norm = jacob.length()/2/view_width;
        } catch (IOException e) {
            e.printStackTrace();
        }
        int first = (int) (T2*norm);
        if (first%2==1){first-=1;}
        int second = (int) (T1*norm);
        if (second%2==1){second-=1;}
        Pair pair=new Pair<>(first,second);
        return pair;
    }

    public void setTables(PieceTable pieceTable, BitmapPieceTable bitmapPieceTable) {
        this.pieceTable = pieceTable;
        this.bitmapPieceTable = bitmapPieceTable;
    }

    private void beEditableGraph(Canvas canvas) {
        if(editable==null){
            editable = bitmapPieceTable.find(0, bitmapPieceTable._text_len,paint,Bitmap.Config.ALPHA_8);
        }
        canvas.drawBitmap(editable, 0, 0, paint);
        if (T2_onScren || T1_onScren){
            canvas.drawLine(T1, view_height/2, T1, 0, y_coordinate_axis);
            canvas.drawLine(T2, view_height/2, T2, 0, y_coordinate_axis);
        }
    }

    public class Drawable{
        public Canvas canvas;
        public Bitmap bitmap;
        public Paint color;
        public Drawable(int width, int height){
            this.bitmap = Bitmap.createBitmap(width,height,
                    Bitmap.Config.ALPHA_8);
            this.canvas = new Canvas(bitmap);

        }

    }

/*
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

             }
        canvas.drawLines(test,paint);
    }

 */
}






//TODO what is ALPHA_8 and does it store and represent bitmap color information within int[] ?
//  _ _ _ _ == _
//             _
//             _
//             _
//  in other words, it stores a summation of unordered line segments that directly correspond to the rows AND NOT the columns of the bitmap
//  So in order to move from rows->columns we must process over the entire bitmap whatever its original length was which is total bummer!
//  the length of a row in this case is T1-T2
//  we could iterate through the summation of rows and append only the data from
//  row_i < x < (T1-T2) / horizontal split
//  what is a column than just a summation of row with width 1
//  conversely, what is a row than just a summation of columns with height 1
//  rows are columns and columns are rows damn you dualism
//  wait! why don't we cheat at rotate the bitmap so that we can think in columns AND NOT rows that would be totally awesome!
//  but then of course we have to rewrite our drawing code
//  that was pointless
//  the int[] from bitmap.getPixels is going to have some issues later so what do these do?
//  bitmap.extractAlpha(paint,offset) = bitmap
//  bitmap.copyPixelsToBuffer();
//

/*
graph_pos = (int) (view_width-(pixel_density*10));
int[] pixels = new int[(int) ((view_width-pixel_density)*view_height)];
liveGraph.bitmap.getPixels(pixels, 0,(int)(view_width-pixel_density), (int) pixel_density,0,(int)(view_width-pixel_density),(int)view_height);
liveGraph.bitmap.setPixels(pixels, 0, (int)(view_width-pixel_density), 0,0,(int)(view_width-pixel_density),(int)view_height);
startGraphing(liveGraph.canvas,invalidate);
*/
/*
int j=0;
for(int i=0; i<selection.length;i++){
    if (j<=(int)(T1-T2)/3) {
        dimens_test[i] = selection[i];
    }
    else if (j>=(int)(T1-T2)){
        j=0;
    }
    j+=1;
}
 */
/*
                (1)     (2)
            <-- [ ] -- |[ ]|

                (2)     (3)
            <-- [ ] -- |[ ]|

               (i-1)    (i)
            <-- [ ] -- |[ ]|

            two different conditions that have symmetric looking sub conditions
 */
  /*
    private void doGraphing(Canvas canvas,Boolean invalidate)
    {
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, x_coordinate_axis);
        if(iter<=view_width){
            if(liveGraph==null){
                x_0 = 0;
                liveGraph = new Drawable((int) view_width, (int)view_height);
            }
            if (liverGraph!=null){
                System.out.println("now draw the second bitmap like you would the first and the new bitmap that we just allocated like you would the second until the second is off screen");
                //4.) "now draw the second bitmap like you would the first and the new bitmap that we just allocated like you would the second until the second is off screen"
                canvas.drawBitmap(liveGraph.bitmap,view_width-graph_pos,0,paint);
                canvas.drawBitmap(liverGraph.bitmap,x_0-=pixel_density,0,paint);
                liveGraph.update_position(view_width-graph_pos);
                liverGraph.update_position(x_0);

            }else{
                System.out.println("empty canvas draw until you fill up the screen");
                //1.) empty canvas draw until you fill up the screen
                canvas.drawBitmap(liveGraph.bitmap,0,0,paint);
                ///BitmapShader live=new BitmapShader(liveGraph.bitmap, Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);
                //paint.setShader(live);
                //canvas.drawRect(new RectF(0,0,0,0),paint);
            }
            startGraphing(liveGraph.canvas);
        }
        if(iter>=view_width) {
            if (liverGraph==null){
                graph_pos=0;
                liverGraph = new Drawable((int) view_width, (int)view_height);
            }
            if (liveGraph.position>=0) {
                System.out.println("canvas got completely filled so move the bitmap backwards until its off screen while doing the same for a new bitmap filling this new bitmap up");
                //2.) canvas got completely filled so move the bitmap backwards until its off screen while doing the same for a new bitmap filling this new bitmap up
                canvas.drawBitmap(liveGraph.bitmap, x_0 -= pixel_density, 0, paint);
                canvas.drawBitmap(liverGraph.bitmap, view_width - graph_pos, 0, paint);
                liverGraph.update_position(view_width - graph_pos);
                startGraphing(liverGraph.canvas);
            }
            else{
                canvas.drawBitmap(liveGraph.bitmap,view_width - graph_pos, 0, paint);
                canvas.drawBitmap(liverGraph.bitmap, x_0 -= pixel_density, 0, paint);
                startGraphing(liveGraph.canvas);
            }
            if (view_width-graph_pos<=0 && liveGraph!=null && liverGraph!=null){
                System.out.println("you filled up two bitmaps which means the first one is completely off screen we can write it to cache");
                liveGraph=null;
                iter=0;
                graph_pos=0;
                invalidate();
            }
        }
        //3.) you filled up two bitmaps which means the first one is completely off screen we can write it to cache
        // and we can reset this process
        /*
                (1)     (2)
            <-- [ ] -- |[ ]|

                (2)     (3)
            <-- [ ] -- |[ ]|

               (i-1)    (i)
            <-- [ ] -- |[ ]|

         *
        /*
    if (view_width-graph_pos<=0 && liveGraph!=null && liverGraph!=null){
            System.out.println("you filled up two bitmaps which means the first one is completely off screen we can write it to cache");
            liveGraph=null;
            iter=0;
            gra
            ph_pos=0;
            invalidate();
        }
         *
    }
    */
    /*
    public void startGraphing(Canvas canvas) {
        byte[] buffer = getAudioChunk(audio_length, (int) audio_length, 1);
        short[] chunk = Convert.bytesToShorts(buffer);
        float[] test = new float[chunk.length * 4];
        iter += pixel_density;
        graph_pos+=pixel_density;
        for (int i = 0; i < chunk.length/256; i ++) {
            short min = chunk[i];
            short max = chunk[i];
            for (int j = 0; j < 256; j++) {
                if (max<chunk[i*256]){
                    max=chunk[i*256];
                }
                if (min<chunk[i*256]){
                    min=chunk[i*256];
                }
            }
            test[i * 4] = graph_pos;
            test[i * 4 + 1] = view_height / 2 + min * (view_height / 65535);
            test[i * 4 + 2] = graph_pos;
            test[i * 4 + 3] = (view_height / 2) - max * (view_height / 65535);
        }
        canvas.drawLines(test, paint);
        invalidate();
    }

     */

    /*
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

             /
             }
        canvas.drawLines(test,paint);
    }

     */
    /*
    private void doGraphing(Canvas canvas,Boolean invalidate)
    {

        //float pos1 = view_width-graph_pos;
        //float pos2 = x_0-=pixel_density;
        canvas.drawLine(0, view_height / 2, view_width, view_height / 2, x_coordinate_axis);
        if(liveGraph==null){
            x_0 = 0;
            liveGraph = new Drawable((int) view_width, (int)view_height);
        }
        if(iter<=view_width && liverGraph!=null){
            canvas.drawBitmap(liveGraph.bitmap,view_width-graph_pos,0,paint);
            canvas.drawBitmap(liverGraph.bitmap,x_0-=pixel_density,0,paint);
            //canvas.drawBitmap(liveGraph.bitmap,pos1,0,paint);
            //canvas.drawBitmap(liverGraph.bitmap,pos2,0,paint);
            startGraphing(liveGraph.canvas);
        }
        else if(iter<=view_width){
            canvas.drawBitmap(liveGraph.bitmap,0,0,paint);
            startGraphing(liveGraph.canvas);
        }
        if(iter>=view_width) {
            if (liverGraph==null){
                graph_pos=0;
                liverGraph = new Drawable((int) view_width, (int)view_height);
            }
            canvas.drawBitmap(liveGraph.bitmap,x_0-=pixel_density,0,paint);
            canvas.drawBitmap(liverGraph.bitmap,view_width-graph_pos,0,paint);
            //canvas.drawBitmap(liveGraph.bitmap,pos2,0,paint);
            //canvas.drawBitmap(liverGraph.bitmap,pos1,0,paint);
            startGraphing(liverGraph.canvas);
            if (view_width-graph_pos<=0){ //into cache for liverGraph if != null
                liveGraph=null;
                iter=0;
                graph_pos=0;
                invalidate();
            }
        }
    }

     */