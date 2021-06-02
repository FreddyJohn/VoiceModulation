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
import java.util.Stack;


public class GraphLogic extends View {
    private static float T1;
    private static float T2;
    private float pixel_density;
    private Paint paint;
    private float view_height;
    private float view_width;
    private boolean graphState = false;
    private Drawable drawable;
    private Editable editable;
    private float graph_pos;
    private RandomAccessFile jacob;
    private Paint y_coordinate_axis;
    private long audio_length;
    private boolean T1_onScreen = false;
    private boolean T2_onScreen = false;
    private float select_pos_x;
    private float select_pos_y;
    private PieceTable bitmapPieceTable;
    private RandomAccessFile randomAccessFile;
    private int buffer_size;
    private int columns_to_write;
    private PieceTable audioPieceTable;
    private int count;
    private int columns_byte_length;
    public Points points;

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
        randomAccessFile = groovy.getWriteObject(false);
        // The pixel_density must be a whole number otherwise we will be writing impossible lengths of pixels without error!
        //pixel_density = 1.0f;
        pixel_density = (float) Math.ceil(Convert.numberToDp(context,1));
        paint = new Paint();
        y_coordinate_axis = new Paint();
        y_coordinate_axis.setColor(Color.rgb(115,115,115));
        y_coordinate_axis.setStyle(Paint.Style.STROKE);
        y_coordinate_axis.setStrokeWidth(pixel_density);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        String name = Environment.getExternalStorageDirectory().getPath()+"/rec.pcm";
        paint.setStrokeWidth(pixel_density);
        IO_RAF funky = new IO_RAF(name);
        jacob = funky.getWriteObject(false);
    }
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        float x = roundToNearestMultiple(evt.getX(),pixel_density);
        float y = roundToNearestMultiple(evt.getY(),pixel_density);
        if (editable != null) {
            switch (evt.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    editable.action_down(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    editable.action_up(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    editable.action_move(x, y);
                    break;
            }
            invalidate();
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
            if(drawable ==null){
                drawable = new Drawable((int) view_height/2, (int)view_width);
            }
            drawable.doGraphing(canvas);
        }
        else if (drawable != null){
            if(editable ==null){
                editable = new Editable();
            }
            editable.beEditableGraph(canvas);
            if(editable.selected_bitmap!=null){
                canvas.drawBitmap(editable.selected_bitmap,select_pos_x,select_pos_y,paint);
            }

        }
    }
    // TODO when we call startGraphing here a side effect is that the graph position is changed
    //  this can cause resizing issue between Editable and Drawable states
    //  because those buffers will be written to the screen for Editable but will have been written over in Drawable from the conditional within doGraphing
    //  this conditional plays with startGraphing where they both alternate the graph position plus minus a pixel density
    //  obviously, calling startGraphing creates a side effect that violates this plus minus a pixel density
    //  so the solution is to reuse the logic you've already thought out within that conditional of doGraphing
    //  in laymen terms we move the Drawable back by the amount of columns that were written
    //  let pixels equal all the pixels within Drawable after catchUp


    public void catchUp(boolean b) {
       if (count!=audioPieceTable._text_len) {
           drawable.startGraphing();
           bitmapPieceTable.add(columns_byte_length,bitmapPieceTable._text_len);
           System.out.println("column_byte_length = " + columns_byte_length);
       }
       this.graphState=b;
    }


    public void setGraphState(int buffer_size,boolean state) {
        this.buffer_size = buffer_size;
        this.graphState=state;
        editable = null;
        postInvalidate();
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

    public void setTables(PieceTable bitmapPieceTable, PieceTable audioPieceTable) {
        this.bitmapPieceTable = bitmapPieceTable;
        this.audioPieceTable = audioPieceTable;
    }

    private class Drawable{
        private final Canvas canvas;
        private Bitmap bitmap;
        private final int[] pixels;
        private final int[] column;
        private final float[] lines;
        private short[] cur_buffer;
        private byte[] column_bytes;
        private int[] pixelsMinusColumns;
        private Drawable(int width, int height){
            this.bitmap = Bitmap.createBitmap(width,height,
                    Bitmap.Config.ALPHA_8);
            this.canvas = new Canvas(bitmap);
            this.cur_buffer = new short[buffer_size];
            this.lines = new float[buffer_size * 4];
            this.pixels = new int[bitmap.getAllocationByteCount()];
            this.column = new int[(int) (pixel_density * bitmap.getWidth())];
            this.pixelsMinusColumns = new int[bitmap.getAllocationByteCount() / 4];
            this.column_bytes = new byte[(int) (pixel_density * bitmap.getWidth()) * 4];
        }
        private void doGraphing(Canvas _canvas){
            if(graph_pos>view_width-pixel_density * (10+columns_to_write)) {
                graph_pos = (int) (view_width-pixel_density * (10+columns_to_write));
                bitmap.getPixels(pixels,0, bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
                pixelsMinusColumns = Arrays.copyOfRange(pixels, (int) (bitmap.getWidth() * pixel_density) * columns_to_write, pixels.length);
                bitmap.setPixels(pixelsMinusColumns,0, bitmap.getWidth(),0,0, bitmap.getWidth(), (int) (bitmap.getHeight()-pixel_density*columns_to_write));
            }
            _canvas.drawBitmap(bitmap,0,0,paint);
            startGraphing();
        }
        private void startGraphing() {
            // TODO verify that buffers.length is equal to the difference between two consecutive startGraphing calls during recording
            byte[] buffers = getRecentBuffers(audio_length, (int) audio_length);
            count += buffers.length;
            float norm = (float)(bitmap.getWidth()) / 65535;
            float x_axis = drawable.bitmap.getWidth() / 2;
            columns_to_write = buffers.length / buffer_size !=0 ? buffers.length / buffer_size : 1;
            columns_byte_length = (buffers.length / buffer_size) * column_bytes.length;
            for (int buffer = 0; buffer < buffers.length; buffer += buffer_size) {
                cur_buffer = Convert.bytesToShorts(Arrays.copyOfRange(buffers, buffer, buffer + buffer_size));
                graph_pos += pixel_density;
                for (int i = 0; i < cur_buffer.length; i++) {
                    lines[i * 4] = x_axis;
                    lines[i * 4 + 1] = graph_pos;
                    lines[i * 4 + 2] = x_axis - cur_buffer[i] * norm;
                    lines[i * 4 + 3] = graph_pos;
                }
                canvas.drawLines(lines, paint);
                bitmap.getPixels(column, 0, bitmap.getWidth(),
                        0, (int) (graph_pos-pixel_density),
                        bitmap.getWidth(), (int) pixel_density);
                try {
                    randomAccessFile.seek(randomAccessFile.length());
                    column_bytes = Convert.intsToBytes(column);
                    randomAccessFile.write(column_bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            invalidate();
        }
        private void refreshDrawable() {
            int[] refresh_pixels = Convert.bytesToInts(
                    bitmapPieceTable.find(0, bitmapPieceTable._text_len));
            bitmap.setPixels(refresh_pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(), (int) graph_pos);
            graph_pos = bitmapPieceTable._text_len/(bitmap.getWidth()*pixel_density*4)*pixel_density;
            count = audioPieceTable._text_len;
            audio_length = count;
        }
    }
    private class Editable{
        private Bitmap editable_bitmap;
        private Bitmap selected_bitmap;
        private Stack<Bitmap> editable_stack;
        private int[] selection;
        private void beEditableGraph(Canvas canvas) {
            if(editable_bitmap ==null){
                refreshEditable();
            }
            canvas.drawBitmap(editable_bitmap, 0, 0, paint);
            if (T2_onScreen || T1_onScreen){
                canvas.drawLine(view_height/2, T1, 0, T1, y_coordinate_axis);
                canvas.drawLine(view_height/2, T2, 0, T2, y_coordinate_axis);
            }
        }
        private void refreshEditable(){
            float cut_off = view_width-pixel_density*10;
            int width = drawable.bitmap.getWidth();
            if(graph_pos<cut_off) {
                int height = (int) (bitmapPieceTable._text_len/(width*pixel_density*4)*pixel_density);
                System.out.println("is the calculated position and real position the same? "+height+" , "+graph_pos);
                int[] refresh = Convert.bytesToInts(
                        bitmapPieceTable.find(0, bitmapPieceTable._text_len));

                editable_bitmap = Bitmap.createBitmap(refresh,
                        width, height,Bitmap.Config.ALPHA_8);
            }
            else {
                int length = (int) ((cut_off*(width)*4));
                int[] refresh = Convert.bytesToInts(
                        bitmapPieceTable.find(bitmapPieceTable._text_len-length, length));

                editable_bitmap = Bitmap.createBitmap(refresh,
                        drawable.bitmap.getWidth(), (int) cut_off,Bitmap.Config.ALPHA_8);
            }
        }
        private void action_down(float x, float y) {
            if(!graphState && y <= view_height / 2 && x <= graph_pos){
                selected_bitmap = null;
                if (!T1_onScreen) {
                    T1 = x;
                    T1_onScreen = true;
                }
                else if (!T2_onScreen && y < T1 - 20) {
                    T2 = x;
                    T2_onScreen = true;
                }
            }
        }

        private void action_up(float x, float y) {
            if(selected_bitmap==null && x>=T2+20 && x <= T1-20 && y<=view_height/2) {
                allocateSelection();
                performClick();
            }
            else if(selected_bitmap!=null && y<=view_height/2 && !T1_onScreen && !T2_onScreen){
                writeSelection(x,y);
                drawable.refreshDrawable();
                performClick();
            }
        }
        private void action_move(float x, float y) {
            if(selected_bitmap==null) {
                if (!graphState && y <= view_height / 2 && x<=graph_pos) {
                    if (T1 - x <= 20 && x >= T2 + 20) {
                        T1 = x;
                    }
                    if (x - T2 <= 20 && x <= T1 - 20) {
                        T2 = x;
                    }
                }
                else if (y>=view_height/2){
                    scrollTo((int)x,(int)(y-view_height/2));
                    scrollTo(0 ,(int)(y-view_height/2));
                    scrollTo((int) x,0);
                }
            }
            else if(y>=view_height/2){
                select_pos_x= -(y-selected_bitmap.getWidth());
                select_pos_y= x;
                T1_onScreen = false;
                T2_onScreen = false;
            }
        }
        private void allocateSelection(){
            selection = new int[(int) (editable_bitmap.getWidth()*(T1-T2))];
            editable_bitmap.getPixels(selection, 0, editable_bitmap.getWidth(), 0, (int)T2, editable_bitmap.getWidth(),  (int) (T1-T2));
            selected_bitmap = Bitmap.createBitmap(selection, editable_bitmap.getWidth(),(int)(T1-T2), Bitmap.Config.ALPHA_8);
            select_pos_x= -view_height/2;
            select_pos_y=T2;
        }
        private void writeSelection(float x, float y){
            points =  new Points((int) T2, (int)T1, (int) x);
            System.out.println("audio points "+points.audio_start+" , "+points.audio_stop+" , "+points.audio_length);
            System.out.println("audio length "+audioPieceTable._text_len);
            System.out.println("action down x "+x);
            byte[] audioByteSelection = audioPieceTable.find(points.audio_start, points.audio_length);
            byte[] bitmapByteSelection = Convert.intsToBytes(selection);
            try {
                jacob.seek(audioPieceTable._text_len);
                randomAccessFile.seek(bitmapPieceTable._text_len);
                jacob.write(audioByteSelection);
                randomAccessFile.write(bitmapByteSelection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmapPieceTable.add(bitmapByteSelection.length, points.bitmap_insert);
            audioPieceTable.add(points.audio_length,points.audio_insert);
            editable_bitmap = null;
            selected_bitmap = null;
        }
    }
    public class Points {
        public int audio_start;
        public int audio_stop;
        public int audio_length;
        public int audio_insert;
        private int bitmap_insert;
        public Points(int start, int stop, int insert){
            this.audio_start = (int) (start * buffer_size / pixel_density);
            this.audio_stop = (int) (stop  * buffer_size / pixel_density);
            this.audio_length = audio_stop - audio_start;
            this.audio_insert =  (int) (insert * buffer_size / pixel_density);
            this.bitmap_insert = (int) (insert * 4 * drawable.bitmap.getWidth());
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
