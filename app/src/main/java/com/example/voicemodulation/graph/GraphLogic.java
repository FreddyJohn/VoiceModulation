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
import com.example.voicemodulation.audio.AudioCon.IO_RAF;
import com.example.voicemodulation.sequence.PieceTable;
import com.example.voicemodulation.util.Convert;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

//TODO DISPLAY BUGS
//  1.) combination of roundToNearestMultiple could result in Piece not found
//          because if you select near the end of both the waveform and a pixel density then (2) will cause incorrect parameters for find/add/delete
//  2.) unit conversation equation issues will cause refresh after insert to decode improperly
//         because the selection was inserted X columns or bytes off

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
    private float graph_pos;
    private Bitmap SelectBitmap;
    private RandomAccessFile jacob;
    private Paint y_coordinate_axis;
    private long audio_length;
    private int[] selection;
    private boolean T1_onScreen = false;
    private boolean T2_onScreen = false;
    private float select_pos_x;
    private float select_pos_y;
    private PieceTable bitmapPieceTable;
    private RandomAccessFile bitmap;
    private int buffer_size;
    private int columns_to_write;
    private PieceTable audioPieceTable;
    private int count;

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
    public void init(Context context)
    {
        setHorizontalScrollBarEnabled(true);
        String namer= Environment.getExternalStorageDirectory().getPath()+"/bitmap";
        IO_RAF groovy = new IO_RAF(namer);
        bitmap = groovy.getWriteObject(false);
        // The pixel_density must be a whole number otherwise we will be writing impossible lengths of pixels without error!
        //pixel_density = 1.0f;
        pixel_density = (float) Math.ceil(Convert.numberToDp(context,1));
        paint = new Paint();
        y_coordinate_axis = new Paint();
        y_coordinate_axis.setColor(Color.rgb(115,115,115));
        y_coordinate_axis.setStyle(Paint.Style.STROKE);
        y_coordinate_axis.setStrokeWidth(pixel_density);
        paint.setColor(Color.RED);
       // System.out.println("THE PIXEL DENSITY IS: "+ Convert.numberToDp(context,1));
        paint.setStyle(Paint.Style.STROKE);
        String name = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
        paint.setStrokeWidth(pixel_density);
        IO_RAF funky = new IO_RAF(name);
        jacob = funky.getWriteObject(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        float x = evt.getX();
        float y = evt.getY();
        switch (evt.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!graphState && y<=view_height/2 && x<=graph_pos){
                    SelectBitmap=null;
                    if (!T1_onScreen) {
                        T1 = x;
                        invalidate();
                        T1_onScreen =true;
                    }
                    else if (!T2_onScreen && y<T1-20) {
                        T2 = x;
                        invalidate();
                        T2_onScreen =true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(SelectBitmap==null && x>=T2+20 && x <= T1-20 && y<=view_height/2) {
                    selection = new int[(int) (editable.getWidth()*(T1-T2))];
                    editable.getPixels(selection, 0, editable.getWidth(), 0, (int)T2, editable.getWidth(),  (int) (T1-T2));
                    SelectBitmap = Bitmap.createBitmap(selection,editable.getWidth(),(int)(T1-T2), Bitmap.Config.ALPHA_8);
                    select_pos_x= -view_height/2;
                    select_pos_y=T2;
                    performClick();
                    invalidate();
                }
                else if(SelectBitmap!=null && y<=view_height/2 && !T1_onScreen && !T2_onScreen){
                    System.out.println("T1 and T2 " + T1 +" , " + T2);
                    Selection points = getSelectionPoints();
                    float pixel = roundToNearestMultiple(x,pixel_density);
                    int selected_index = bitmapPieceTable._text_len;
                            //(int) (pixel * (liveGraph.bitmap.getWidth() * pixel_density));
                    System.out.println("ext.getX() = " + pixel);
                    System.out.println("graph_pos = "+ graph_pos);
                    System.out.println("selected_index = "+ selected_index);
                    System.out.println("points = "+points.audio_start+" , "+points.audio_stop+" , "+points.audio_length);
                    System.out.println("bitmap_byte_length = "+ bitmapPieceTable._text_len);
                    System.out.println("audio_byte_length = "+ audioPieceTable._text_len);
                    byte[] audioByteSelection =audioPieceTable.find(points.audio_start, points.audio_length);
                    byte[] bitmapByteSelection = Convert.intsToBytes(selection);
                    try {
                        jacob.seek(audioPieceTable._text_len);
                        bitmap.seek(bitmapPieceTable._text_len);
                        jacob.write(audioByteSelection);
                        bitmap.write(bitmapByteSelection);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmapPieceTable.add(bitmapByteSelection.length, (selected_index));
                    audioPieceTable.add(points.audio_length,audioPieceTable._text_len);
                    editable=null;
                    SelectBitmap = null;
                    performClick();
                    invalidate();
                    }
                break;
            case MotionEvent.ACTION_MOVE:
                if(SelectBitmap==null) {
                    if (!graphState && y <= view_height / 2 && x<=graph_pos) {
                        if (T1 - x <= 20 && x >= T2 + 20) {
                            T1 = x;
                            //System.out.println("T1=" + T1);
                            invalidate();
                        }
                        if (x - T2 <= 20 && x <= T1 - 20) {
                            T2 = x;
                            //System.out.println("T2=" + T2);
                            invalidate();
                        }
                    }
                }
                else if(evt.getY()>=view_height/2 && SelectBitmap != null ){
                    select_pos_x= -(evt.getY()-SelectBitmap.getWidth());
                    select_pos_y= evt.getX();
                    T1_onScreen = false;
                    T2_onScreen = false;
                    invalidate();
                }
              //  else if (evt.getY()>=view_height/2){
                //scrollTo((int)evt.getX(),(int)(evt.getY()-view_height/2));
                //scrollTo(0 ,(int)(evt.getY()-view_height/2));
                //scrollTo((int) evt.getX(),0);

                //}
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
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
        canvas.translate(0,+(int)(view_height/2));
        canvas.rotate(-90);
        if(graphState) {
            doGraphing(canvas);
        }
        else if (liveGraph != null){
            beEditableGraph(canvas);
            if(SelectBitmap!=null){
                canvas.drawBitmap(SelectBitmap,select_pos_x,select_pos_y,paint);
            }

        }
    }

    private void doGraphing(Canvas canvas){
        if(graph_pos>view_width-pixel_density * 10) {
            graph_pos = (int) (view_width-pixel_density * 10);
            liveGraph.bitmap.getPixels(liveGraph.pixels,0,liveGraph.bitmap.getWidth(),0,0,liveGraph.bitmap.getWidth(),liveGraph.bitmap.getHeight());
            liveGraph.pixelsMinusColumns = Arrays.copyOfRange(liveGraph.pixels, (int) (liveGraph.bitmap.getWidth() * pixel_density) * columns_to_write, liveGraph.pixels.length);
            liveGraph.bitmap.setPixels(liveGraph.pixelsMinusColumns,0, liveGraph.bitmap.getWidth(),0,0, liveGraph.bitmap.getWidth(), (int) (liveGraph.bitmap.getHeight()-pixel_density*columns_to_write));
        }
        else if(graph_pos<view_width - pixel_density * 10) {
            if(liveGraph==null){
                liveGraph = new Drawable((int) view_height/2, (int)view_width);
            }
        }
        canvas.drawBitmap(liveGraph.bitmap,0,0,paint);
        startGraphing(liveGraph.canvas);
    }

    public void catchUp(boolean b) {
       if (count!=audioPieceTable._text_len) {
           int size =(audioPieceTable._text_len-count)/buffer_size;
           for (int i = 0; i < size; i ++) {
               startGraphing(liveGraph.canvas);
           }
       }
       this.graphState=b;
    }

    public void setGraphState(int buffer_size,boolean state) {
        this.buffer_size = buffer_size;
        this.graphState=state;
        editable = null;
        postInvalidate();
    }

    private void startGraphing(Canvas canvas) {
        byte[] buffers = getRecentBuffers(audio_length, (int) audio_length);
        this.count += buffers.length;
        float norm = (float)(liveGraph.bitmap.getWidth()) / 65535;
        float x_axis = liveGraph.bitmap.getWidth() / 2;
        columns_to_write = buffers.length / buffer_size !=0 ? buffers.length / buffer_size : 1;
        for (int buffer = 0; buffer < buffers.length; buffer += buffer_size) {
            liveGraph.cur_buffer = Convert.bytesToShorts(Arrays.copyOfRange(buffers, buffer, buffer + buffer_size));
            graph_pos += pixel_density;
            for (int i = 0; i < liveGraph.cur_buffer.length; i++) {
                liveGraph.lines[i * 4] = x_axis;
                liveGraph.lines[i * 4 + 1] = graph_pos;
                liveGraph.lines[i * 4 + 2] = x_axis - liveGraph.cur_buffer[i] * norm;
                liveGraph.lines[i * 4 + 3] = graph_pos;
            }
            canvas.drawLines(liveGraph.lines, paint);
            liveGraph.bitmap.getPixels(liveGraph.column, 0, liveGraph.bitmap.getWidth(),
                    0, (int) (graph_pos-pixel_density),
                    liveGraph.bitmap.getWidth(), (int) pixel_density);
            try {
                bitmap.seek(bitmap.length());
                liveGraph.column_bytes = Convert.intsToBytes(liveGraph.column);
                bitmap.write(liveGraph.column_bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        invalidate();
    }

    private byte[] getRecentBuffers(long file_position, int chunk_size){
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
        byte[] buffer = new byte[Math.abs((length)-chunk_size)];
        try {
            jacob.read(buffer);
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        this.audio_length = length;
        return  buffer;
    }
    public Selection getSelectionPoints(){
        return new Selection((int) T2, (int)T1);
    }

    public void setTables(PieceTable bitmapPieceTable, PieceTable audioPieceTable) {
        this.bitmapPieceTable = bitmapPieceTable;
        this.audioPieceTable = audioPieceTable;
    }

    private void beEditableGraph(Canvas canvas) {
        if(editable==null){
            System.out.println("are these two numbers the same? " + count+" , "+audioPieceTable._text_len);
            System.out.println("count always less than? " + (count<audioPieceTable._text_len));
            System.out.println("audio_length = " + audio_length);
            refreshDrawable();
        }
        canvas.drawBitmap(editable, 0, 0, paint);
        if (T2_onScreen || T1_onScreen){
            canvas.drawLine(view_height/2, T1, 0, T1, y_coordinate_axis);
            canvas.drawLine(view_height/2, T2, 0, T2, y_coordinate_axis);
        }
    }
    private void refreshDrawable(){
        float cut_off = view_width-pixel_density*10;
        int width = liveGraph.bitmap.getWidth();
        if(graph_pos<cut_off) {
            int height = (int) (bitmapPieceTable._text_len/(width*pixel_density*4)*pixel_density);
            System.out.println("is the calculated position and real position the same? "+height+" , "+graph_pos);
            int[] refresh = Convert.bytesToInts(bitmapPieceTable.find(0, bitmapPieceTable._text_len));
            editable = Bitmap.createBitmap(refresh,
                    width, height,Bitmap.Config.ALPHA_8);
        }else {
            int length = (int) ((cut_off*(width)*4));
            int[] refresh = Convert.bytesToInts(bitmapPieceTable.find(bitmapPieceTable._text_len-length, length));
            editable = Bitmap.createBitmap(refresh,
                    liveGraph.bitmap.getWidth(), (int) cut_off,Bitmap.Config.ALPHA_8);

        }
    }
    private class Drawable{
        private Canvas canvas;
        private Bitmap bitmap;
        private int[] pixels;
        private int[] column;
        private float[] lines;
        private short[] cur_buffer;
        private byte[] column_bytes;
        private int[] pixelsMinusColumns;
        public Drawable(int width, int height){
            this.bitmap = Bitmap.createBitmap(width,height,
                    Bitmap.Config.ALPHA_8);
            this.canvas = new Canvas(bitmap);
            this.cur_buffer = new short[buffer_size];
            this.lines = new float[buffer_size * Float.BYTES];
            this.pixels = new int[bitmap.getAllocationByteCount()];
            this.column = new int[(int) (pixel_density * bitmap.getWidth())];
            this.pixelsMinusColumns = new int[bitmap.getAllocationByteCount()];
            this.column_bytes = new byte[(int) (pixel_density * bitmap.getWidth()) * Float.BYTES];
        }
    }
    public class Selection{
        public int audio_start;
        public int audio_stop;
        public int audio_length;
        public Selection (int start, int stop){
            this.audio_start = (int) (roundToNearestMultiple(start,pixel_density) * buffer_size / pixel_density);
            this.audio_stop = (int) (roundToNearestMultiple(stop,pixel_density)  * buffer_size / pixel_density);
            this.audio_length = audio_stop - audio_start;
        }
    }
    public int roundToNearestMultiple(float num, float multiple){
        return (int) (multiple*(Math.ceil(Math.abs(num/multiple))));
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
