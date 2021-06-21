package com.example.voicemodulation.graph;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.voicemodulation.project.Paths;
import com.example.voicemodulation.audio.AudioConnect.IO_RAF;
import com.example.voicemodulation.sequence.PieceTable;
import com.example.voicemodulation.util.Convert;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class GraphLogic extends View {
    public static float T1;
    public static float T2;
    private Paths projectPaths;
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
    public BytePoints points;
    private int record_session_length;
    private int bitmap_session_length;
    private float draw_pos;
    private float s1;
    private float s2;

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
        pixel_density = (float) Math.ceil(Convert.numberToDp(context, 1));
        paint = new Paint();
        y_coordinate_axis = new Paint();
        y_coordinate_axis.setColor(Color.rgb(115, 115, 115));
        y_coordinate_axis.setStyle(Paint.Style.STROKE);
        y_coordinate_axis.setStrokeWidth(pixel_density);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(pixel_density);
    }
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        if (editable != null) {
            float x = roundToNearestMultiple(evt.getX(), pixel_density);
            float y = roundToNearestMultiple(evt.getY(), pixel_density);
            points = new BytePoints((int) s2, (int) s1, (int) (draw_pos+x));
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
        this.buffer_size = buffer_size;
        this.graphState = state;
        editable = null;
        scrollTo(0,0);
        if (!graphState ){
            int audio_file_length = 0;
            int bitmap_file_length = 0;
            try{
                audio_file_length = (int) jacob.length();
                bitmap_file_length = (int) randomAccessFile.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (audioPieceTable.byte_length == 0) { // TODO if null then create and add instead of passing fro MainActivity!
                audioPieceTable.add_original(audio_file_length);
                bitmapPieceTable.add_original(bitmap_file_length);
                closeFiles();
                IO_RAF funky = new IO_RAF(projectPaths.audio);
                jacob = funky.getWriteObject(false);
                IO_RAF groovy = new IO_RAF(projectPaths.bitmap);
                randomAccessFile = groovy.getWriteObject(false);
                file_length=0;

            } else {
                audioPieceTable.add(record_session_length, audioPieceTable.byte_length);
                bitmapPieceTable.print_pieces();
                bitmapPieceTable.add(bitmap_session_length, bitmapPieceTable.byte_length);
            }
            record_session_length = 0;
            bitmap_session_length = 0;
        }
        postInvalidate();
    }
    public void setTables(PieceTable bitmapPieceTable, PieceTable audioPieceTable) {
        this.bitmapPieceTable = bitmapPieceTable;
        this.audioPieceTable = audioPieceTable;
    }
    public void setProjectPaths(Paths projectPaths){
        this.projectPaths = projectPaths;
        IO_RAF funky = new IO_RAF(projectPaths.audio_original);
        this.jacob = funky.getWriteObject(true);
        IO_RAF groovy = new IO_RAF(projectPaths.bitmap_original);
        this.randomAccessFile = groovy.getWriteObject(true);
    }
    private void closeFiles() {
        try {
            jacob.close();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public class BytePoints {
        public int audio_start;
        public int audio_stop;
        public int audio_length;
        public int audio_insert;
        public int bitmap_insert;
        public int bitmap_start;
        public BytePoints(int start, int stop, int insert) {
            System.out.println("start="+start+" stop="+stop+" insert="+insert);
            this.audio_start = (int) (start * buffer_size / pixel_density);
            this.audio_stop = (int) (stop * buffer_size / pixel_density);
            this.audio_length = audio_stop - audio_start;
            this.audio_insert = (insert * buffer_size / pixel_density) >= audioPieceTable.byte_length ?
                    audioPieceTable.byte_length : (int) (insert * buffer_size / pixel_density);
            this.bitmap_insert = (insert * 4 * drawable.bitmap.getWidth()) >= bitmapPieceTable.byte_length ?
                    bitmapPieceTable.byte_length : (insert * 4 * drawable.bitmap.getWidth());
            this.bitmap_start = (start * drawable.bitmap.getWidth() * 4);
        }
    }
    public int roundToNearestMultiple(float num, float multiple) {
        return (int) (multiple * (Math.ceil(Math.abs(num / multiple))));
    }
    private class Drawable {
        private  Canvas canvas;
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
            graph_pos = bitmapPieceTable.byte_length / (bitmap.getWidth()  * 4);
            //int[] refresh_pixels = Convert.bytesToInts(
            //        bitmapPieceTable.find(0, bitmapPieceTable._text_len));
            int[] refresh_pixels = Convert.bytesToInts(bitmapPieceTable.get_text());
            this.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ALPHA_8);
            this.canvas = new Canvas(bitmap);
            this.bitmap.setPixels(refresh_pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), (int) graph_pos);
            count = audioPieceTable.byte_length;
            try {
                file_length = (int) jacob.length();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class Editable{
        private Bitmap editable_bitmap;
        private Bitmap selected_bitmap;
        private long height;
        private int[] selection;
        private float x1;
        private float x2;
        private long t1;
        private long t2;


        private void beEditableGraph(Canvas canvas) {
            if(editable_bitmap == null){
                refreshEditable();
                height = (bitmapPieceTable.byte_length / (drawable.bitmap.getWidth() * 4));
                draw_pos = graph_pos >= drawable.cut_off ? height-drawable.cut_off : 0;
            }
            if (editable.selected_bitmap != null) {
                canvas.drawBitmap(editable.selected_bitmap, select_pos_x, select_pos_y, paint);
            }
            if(editable_bitmap!=null) {
                if (graph_pos >= drawable.cut_off) {
                    canvas.drawBitmap(editable_bitmap, 0, drawable.cut_off - height, paint);
                    if (T2_onScreen || T1_onScreen) {
                        canvas.drawLine(view_height / 2, T1, 0, T1, y_coordinate_axis);
                        canvas.drawLine(view_height / 2, T2, 0, T2, y_coordinate_axis);
                    }
                } else if (graph_pos <= drawable.cut_off) {
                    canvas.drawBitmap(editable_bitmap, 0, 0, paint);
                    if (T2_onScreen || T1_onScreen) {
                        canvas.drawLine(view_height / 2, T1, 0, T1, y_coordinate_axis);
                        canvas.drawLine(view_height / 2, T2, 0, T2, y_coordinate_axis);
                    }
                }
            }
        }
        //TODO use find operation to implement render based on user locality.
        private void refreshEditable(){
            int width = drawable.bitmap.getWidth();
            int height1 =  bitmapPieceTable.byte_length /(width*4);
            int[] refresh = Convert.bytesToInts(bitmapPieceTable.get_text());
            editable_bitmap = Bitmap.createBitmap(refresh,
                    width, height1,Bitmap.Config.ALPHA_8);
        }

        private void action_down(float x, float y) {
            x1 = x;
            t1=System.nanoTime();
            if(!graphState && y <= view_height / 2){
                selected_bitmap = null;
                if (!T1_onScreen) {
                    T1 = scroll_pos+x;
                    s1 = draw_pos+x;
                    T1_onScreen = true;
                }
                else if (!T2_onScreen && y < T1 - 20) {
                    T2 = scroll_pos+x;
                    s2 = draw_pos+x;
                    T2_onScreen = true;
                }
            }
        }
        private void action_up(float x, float y) {
            float position = Math.abs(draw_pos+x);
            float upper_bound = Math.abs(s2+20);
            float lower_bound = Math.abs(s1-20);
            System.out.println("position="+position+" upper_bound="+upper_bound+" lower_bound="+lower_bound);
            if(selected_bitmap==null && position>=upper_bound && position <= lower_bound && y<=view_height/2) {
                allocateSelection();
                System.out.println("draw_pos="+draw_pos+" scroll_pos="+scroll_pos);
            }
            else if(selected_bitmap!=null && !T1_onScreen && !T2_onScreen) {
                if(y <= view_height / 2){
                    writeSelection();
                    drawable.refreshDrawable();
                }
                else if(y>=view_height){
                    removeSelection();
                    drawable.refreshDrawable();
                }
            }
        }
        private void action_move(float x, float y) {
            if(selected_bitmap==null) {
                if (!graphState && y <= view_height / 2 ){
                    if (T1 - (scroll_pos+x) <= 20 && (scroll_pos+x) >= T2 + 20) {
                        T1 = scroll_pos+x;
                        s1 = draw_pos+x;
                        System.out.println("s1="+s1);
                        System.out.println("T1="+T1);
                    }
                    if ((scroll_pos+x) - T2 <= 20 && (scroll_pos+x) <= T1 - 20) {
                        T2 = scroll_pos+x;
                        s2 = draw_pos+x;
                        System.out.println("s2="+s2);
                        System.out.println("T2="+T2);
                    }
                }
                else if (y>=view_height/2){
                    x2=x;
                    t2=System.nanoTime();
                    double speed = Math.abs(((x2-x1)/((t2-t1)/1E+9)));
                    float norm = drawable.cut_off;
                    int sensitivity = (int) (speed/norm)!=0 ? (int) (speed/norm) : 1;
                    double exponential = Math.pow(pixel_density,sensitivity);
                    if(x1>x2 && scroll_pos != (drawable.cut_off-height) && scroll_pos-exponential>=(drawable.cut_off-height)){
                        scroll_pos -= exponential;
                        draw_pos -= exponential;
                    }else if(x2>x1 && scroll_pos!=0 && scroll_pos+exponential<=0){
                        scroll_pos += exponential;
                        draw_pos += exponential;
                    }
                    scrollTo((int) scroll_pos, 0);
                }
            }
            else if(y>=view_height/2){
                select_pos_x= -(y-selected_bitmap.getWidth());
                select_pos_y= scroll_pos+x;
                T1_onScreen = false;
                T2_onScreen = false;
            }
        }
        private void allocateSelection(){
            // TODO  java.lang.IllegalArgumentException: y + height must be <= bitmap.height() -> "selected_bitmap = Bitmap.createBitmap..."
            // TODO  java.lang.IllegalArgumentException: y + height must be <= bitmap.height() -> "editable_bitmap.getPixels..."
            try {
                //selection = new int[(int) (editable_bitmap.getWidth()*(T1-T2))];
                int selection_length = (int) Math.abs(T1-T2);
                System.out.println("allocating selection T1="+T1+" T2="+T2+" (Math.abs(T1-T2))="+selection_length);
                selection = new int[(int) (editable_bitmap.getWidth()*selection_length)];
                editable_bitmap.getPixels(selection, 0, editable_bitmap.getWidth(), 0, (int) T2, editable_bitmap.getWidth(), selection_length);
                selected_bitmap = Bitmap.createBitmap(selection, editable_bitmap.getWidth(), selection_length, Bitmap.Config.ALPHA_8);
                select_pos_x= -view_height/2;
                select_pos_y=T2;
            } catch (IllegalArgumentException e){ }
        }
        private void writeSelection(){
            byte[] audioByteSelection = audioPieceTable.find(points.audio_start, points.audio_length);
            byte[] bitmapByteSelection = Convert.intsToBytes(selection);
            try {
                jacob.seek(jacob.length());
                randomAccessFile.seek(randomAccessFile.length());
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
            bitmapPieceTable.remove(points.bitmap_start,selection.length*4);
            audioPieceTable.remove(points.audio_start,points.audio_length);
            editable_bitmap = null;
            selected_bitmap = null;
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