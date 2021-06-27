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

public class GraphLogic extends View {
    public static float T1;
    public static float T2;
    public float pixel_density;
    public Paint paint;
    public float view_height;
    public float view_width;
    public boolean graphState = false;
    public Drawable drawable;
    private Editable editable;
    public float graph_pos;
    public float scroll_pos;
    public RandomAccessFile jacob;
    public Paint y_coordinate_axis;
    public long file_length;
    public boolean T1_onScreen = false;
    public boolean T2_onScreen = false;
    public float select_pos_x;
    public float select_pos_y;
    public PieceTable bitmapPieceTable;
    public RandomAccessFile randomAccessFile;
    public int buffer_size;
    public int columns_to_write;
    public PieceTable audioPieceTable;
    public int count;
    public int columns_byte_length;
    public Points points;
    private boolean removed;
    private int record_session_length;
    private int bitmap_session_length;
    private int record_removed_length;
    private int bitmap_removed_length;

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

    public void init(Context context) {
        setHorizontalScrollBarEnabled(true);
        String namer = Environment.getExternalStorageDirectory().getPath() + "/bitmap";
        IO_RAF groovy = new IO_RAF(namer);
        randomAccessFile = groovy.getWriteObject(false);
        // The pixel_density must be a whole number otherwise we will be writing impossible lengths of pixels without error!
        //pixel_density = 1.0f;
        pixel_density = (float) Math.ceil(Convert.numberToDp(context, 1));
        paint = new Paint();
        y_coordinate_axis = new Paint();
        y_coordinate_axis.setColor(Color.rgb(115, 115, 115));
        y_coordinate_axis.setStyle(Paint.Style.STROKE);
        y_coordinate_axis.setStrokeWidth(pixel_density);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        String name = Environment.getExternalStorageDirectory().getPath() + "/rec.pcm";
        paint.setStrokeWidth(pixel_density);
        IO_RAF funky = new IO_RAF(name);
        jacob = funky.getWriteObject(false);
    }
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        if (editable != null) {
            float x = roundToNearestMultiple(evt.getX(), pixel_density);
            float y = roundToNearestMultiple(evt.getY(), pixel_density);
            points = new Points((int) T2, (int) T1, (int) x);
            switch (evt.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    editable.action_down(x, y);
                    performClick();
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
        canvas.translate(0, +(int) (view_height / 2));
        canvas.rotate(-90);
        if (graphState) {
            if (drawable == null) {
                drawable = new Drawable((int) view_height/2, (int)view_width);
            }
            drawable.doGraphing(canvas);
        } else if (drawable != null) {
            if (editable == null) {
                editable = new Editable();
            }
            editable.beEditableGraph(canvas);
            System.out.println("in onDraw bitmapPieceTable._text_len="+bitmapPieceTable._text_len+" graph_pos="+graph_pos);
            if (editable.selected_bitmap != null) {
                canvas.drawBitmap(editable.selected_bitmap, select_pos_x, select_pos_y, paint);
            }
        }
    }
    public void catchUp(boolean b) {
        /*
        if (count != audioPieceTable._text_len) {
            drawable.startGraphing();
            bitmapPieceTable.add(columns_byte_length, bitmapPieceTable._text_len);
            if (graph_pos >= view_width - pixel_density * (10 + columns_to_write)) {
                int columns = columns_byte_length / drawable.column_bytes.length;
                drawable.bitmap.getPixels(drawable.pixels, 0, drawable.bitmap.getWidth(), 0, 0, drawable.bitmap.getWidth(), drawable.bitmap.getHeight());
                drawable.pixelsMinusColumns = Arrays.copyOfRange(drawable.pixels, (int) (drawable.bitmap.getWidth() * pixel_density) * columns, drawable.pixels.length);
                drawable.bitmap.setPixels(drawable.pixelsMinusColumns, 0, drawable.bitmap.getWidth(), 0, 0, drawable.bitmap.getWidth(), (int) (drawable.bitmap.getHeight() - pixel_density * columns));
                graph_pos = (int) (view_width - pixel_density * (10 + columns_to_write)) + pixel_density;
            }
        }
        */
        this.graphState = b;
    }
    public void setGraphState(int buffer_size, boolean state) {
        System.out.println("Pause was pressed during a recording session and graph_pos = "+graph_pos);
        this.buffer_size = buffer_size;
        this.graphState = state;
        editable = null;
        if (!graphState ){
            System.out.println("pause was pressed therefore !graphState");
            long length =0;
            long blength =0;
            try {
                length= jacob.length();
                blength = randomAccessFile.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (audioPieceTable._text_len == 0) {
                System.out.println("We are going to add the original bitmap piece of byte length "+blength);
                System.out.println("We are going to add the original audio piece of byte length "+length);
                audioPieceTable.add_original((int) length);
                bitmapPieceTable.add_original((int) blength);
            }
            else if (!removed) {
                    audioPieceTable.add((int) (length - audioPieceTable._text_len), audioPieceTable._text_len);
                    //audioPieceTable.add(record_session_length, audioPieceTable._text_len);
                    bitmapPieceTable.add((int) blength - bitmapPieceTable._text_len, bitmapPieceTable._text_len);
                    //bitmapPieceTable.add(bitmap_session_length, bitmapPieceTable._text_len);
                    bitmapPieceTable.print_pieces();
                    System.out.println("what is the bitmap_session_length=="+(bitmap_session_length));
                    System.out.println("what is  blength - bitmapPieceTable._text_len=="+(blength - bitmapPieceTable._text_len));
            } else if (removed) {
                    audioPieceTable.add(record_session_length-record_removed_length, audioPieceTable._text_len);
                    bitmapPieceTable.print_pieces();
                    bitmapPieceTable.add(bitmap_session_length-bitmap_removed_length, bitmapPieceTable._text_len);
                    System.out.println("what is the bitmap_session_length="+(bitmap_session_length-bitmap_removed_length));
                    record_removed_length = 0;
                    bitmap_removed_length = 0;
                    removed=false;
            }
            record_session_length = 0;
            bitmap_session_length = 0;
        }
        postInvalidate();
    }
    private byte[] getRecentBuffers(long file_position, int chunk_size) {
        try {
            jacob.seek(file_position);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int length = 0;
        try {
            length = (int) jacob.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[Math.abs((length) - chunk_size)];
        try {
            jacob.read(buffer);
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        this.file_length = length;
        return buffer;
    }
    public void setTables(PieceTable bitmapPieceTable, PieceTable audioPieceTable) {
        this.bitmapPieceTable = bitmapPieceTable;
        this.audioPieceTable = audioPieceTable;
    }
    public class Points {
        public int audio_start;
        public int audio_stop;
        public int audio_length;
        public int audio_insert;
        public int bitmap_insert;
        public int bitmap_start;
        public Points(int start, int stop, int insert) {
            this.audio_start = (int) (start * buffer_size / pixel_density);
            this.audio_stop = (int) (stop * buffer_size / pixel_density);
            this.audio_length = audio_stop - audio_start;
            this.audio_insert = (insert * buffer_size / pixel_density) >= audioPieceTable._text_len ?
                    audioPieceTable._text_len : (int) (insert * buffer_size / pixel_density);
            this.bitmap_insert = (insert * 4 * drawable.bitmap.getWidth()) >= bitmapPieceTable._text_len ?
                    bitmapPieceTable._text_len : (insert * 4 * drawable.bitmap.getWidth());
            this.bitmap_start = (start * drawable.bitmap.getWidth() * 4);
        }
    }
    public int roundToNearestMultiple(float num, float multiple) {
        return (int) (multiple * (Math.ceil(Math.abs(num / multiple))));
    }
    private class Drawable {
        private final Canvas canvas;
        private float cut_off;
        private Bitmap bitmap;
        private final int[] pixels;
        private final int[] column;
        private final float[] lines;
        private short[] cur_buffer;
        private byte[] column_bytes;
        private int[] pixelsMinusColumns;
        private Drawable(int width, int height) {
            this.bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ALPHA_8);
            this.canvas = new Canvas(bitmap);
            this.cur_buffer = new short[buffer_size];
            this.lines = new float[buffer_size * 4];
            this.pixels = new int[bitmap.getAllocationByteCount()];
            this.column = new int[(int) (pixel_density * bitmap.getWidth())];
            this.pixelsMinusColumns = new int[bitmap.getAllocationByteCount() / 4];
            this.column_bytes = new byte[(int) (pixel_density * bitmap.getWidth()) * 4];
        }
        private void doGraphing(Canvas _canvas) {
            if (graph_pos > view_width - pixel_density * (10 + columns_to_write)) {
                graph_pos = (int) (view_width - pixel_density * (10 + columns_to_write));
                bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                pixelsMinusColumns = Arrays.copyOfRange(pixels, (int) (bitmap.getWidth() * pixel_density) * columns_to_write, pixels.length);
                bitmap.setPixels(pixelsMinusColumns, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), (int) (bitmap.getHeight() - pixel_density * columns_to_write));
            }
            cut_off = (view_width - pixel_density * (10 + columns_to_write));
            _canvas.drawBitmap(bitmap, 0, 0, paint);
            startGraphing();
        }
        private void startGraphing() {
            byte[] buffers = getRecentBuffers(file_length, (int) file_length);
            count += buffers.length;
            record_session_length += buffers.length;
            float norm = (float) (bitmap.getWidth()) / 65535;
            float x_axis = drawable.bitmap.getWidth() / 2;
            columns_to_write = buffers.length / buffer_size != 0 ? buffers.length / buffer_size : 1;
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
                        0, (int) (graph_pos - pixel_density),
                        bitmap.getWidth(), (int) pixel_density);
                try {
                    randomAccessFile.seek(randomAccessFile.length());
                    column_bytes = Convert.intsToBytes(column);
                    randomAccessFile.write(column_bytes);
                    bitmap_session_length += column_bytes.length;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            invalidate();
        }
        private void refreshDrawable() {
            graph_pos = bitmapPieceTable._text_len / (bitmap.getWidth()  * 4);
            System.out.println("in refreshDrawable bitmapPieceTable._text_len="+bitmapPieceTable._text_len+" graph_pos="+graph_pos);
            System.out.println("in refreshDrawable audioPieceTable._text_len="+audioPieceTable._text_len);
            int[] refresh_pixels = Convert.bytesToInts(
                    bitmapPieceTable.find(0, bitmapPieceTable._text_len));
            //this.bitmap = Bitmap.createBitmap((int) (view_height/2), (int) view_width,
            //        Bitmap.Config.ALPHA_8);
            this.bitmap.setPixels(refresh_pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), (int) graph_pos);
            count = audioPieceTable._text_len;

        }
    }
    private class Editable{
        private Bitmap editable_bitmap;
        private Bitmap selected_bitmap;
        private long height;
        private int[] selection;
        private void beEditableGraph(Canvas canvas) {
            if(editable_bitmap ==null){
                refreshEditable();
            }
            height = (bitmapPieceTable._text_len / (drawable.bitmap.getWidth() * 4));
            if (graph_pos>=drawable.cut_off) {
                canvas.drawBitmap(editable_bitmap, 0, drawable.cut_off - height, paint);
            }
            else if(graph_pos<=drawable.cut_off){
                canvas.drawBitmap(editable_bitmap, 0, 0, paint);
            }
            if (T2_onScreen || T1_onScreen){
                canvas.drawLine(view_height/2, T1, 0, T1, y_coordinate_axis);
                canvas.drawLine(view_height/2, T2, 0, T2, y_coordinate_axis);
            }
        }
        private void refreshEditable(){
            int width = drawable.bitmap.getWidth();
            System.out.println("In refreshEditable and drawable.bitmap.getWidth()=="+width);
            if(graph_pos<drawable.cut_off) {
                int height =  bitmapPieceTable._text_len/(width*4);
                System.out.println("in refreshEditable and bitmapPieceTable._text_len="+bitmapPieceTable._text_len);
                int[] refresh = Convert.bytesToInts(
                        bitmapPieceTable.find(0, bitmapPieceTable._text_len));
                editable_bitmap = Bitmap.createBitmap(refresh,
                        width, height,Bitmap.Config.ALPHA_8);
            }
            else {
                int length = bitmapPieceTable._text_len/(width*4);
                System.out.println("In refreshEditable and bitmapPieceTable._text_len/(width*4)=="+length);
                //int length = (int) ((cut_off*(width)*4));
                int[] refresh = Convert.bytesToInts(bitmapPieceTable.get_text());
                //int[] refresh = Convert.bytesToInts(
                //        bitmapPieceTable.find(bitmapPieceTable._text_len-length, length));
                editable_bitmap = Bitmap.createBitmap(refresh,
                        width, length,Bitmap.Config.ALPHA_8);
            }
        }
        private void action_down(float x, float y) {
            if(!graphState && y <= view_height / 2){
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
            System.out.println(y);
            if(selected_bitmap==null && x>=T2+20 && x <= T1-20 && y<=view_height/2) {
                allocateSelection();
            }
            else if(selected_bitmap!=null && !T1_onScreen && !T2_onScreen) {
                if(y <= view_height / 2){
                    writeSelection();
                    drawable.refreshDrawable();
                    removed = false;
                    file_length = count;
                    System.out.println("We have written a selection");
                }
                else if(y>=view_height){
                    removeSelection();
                    drawable.refreshDrawable();
                    removed = true;
                    try {
                        file_length = (int) jacob.length();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("We have removed a selection");
                }
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
                    scroll_pos = -x * (height / drawable.cut_off);
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
        private void writeSelection(){
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
        private void removeSelection() {
            System.out.println("in removeSelection bitmapPieceTable._text_len="+bitmapPieceTable._text_len+" graph_pos="+graph_pos);
            System.out.println("selection.length=="+selection.length*4);
            bitmapPieceTable.remove(points.bitmap_start,selection.length*4);
            audioPieceTable.remove(points.audio_start,points.audio_length);
            bitmap_removed_length += selection.length * 4;
            record_removed_length += points.audio_length;
            System.out.println("in removeSelection bitmapPieceTable._text_len="+bitmapPieceTable._text_len+" graph_pos="+graph_pos);
            System.out.println("in removeSelection audioPieceTable._text_len="+audioPieceTable._text_len);
            editable_bitmap = null;
            selected_bitmap = null;
        }
    }
        /*
            else {
        int length = (int) ((cut_off*(width)*4));
        int[] refresh = Convert.bytesToInts(
                bitmapPieceTable.find(bitmapPieceTable._text_len-length, length));

        editable_bitmap = Bitmap.createBitmap(refresh,
                drawable.bitmap.getWidth(), (int) cut_off,Bitmap.Config.ALPHA_8);
    }
         */
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
