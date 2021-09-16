package com.example.voicemodulation.controls;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.voicemodulation.R;

import java.util.Locale;

public class Controller extends LinearLayout {
    private TextView status;
    private TextView title;
    private ControlBar param;
    private String type;
    private double scale;
    private boolean zeroCase = true;
    private ControlCases.seekers quantityToType;
    public  Controller(Context context, @Nullable AttributeSet attrs,String type,double scale){
        super(context,attrs);
        this.scale = scale;
        this.type = type;
        init(context,attrs);}
    public Controller(Context context, @Nullable AttributeSet attrs){
        super(context,attrs);
        init(context,attrs);
    }
    public void init(Context context, @Nullable AttributeSet attrs) {
        param = new ControlBar(context,null);
        title = new TextView(context);
        title.setTextAppearance(R.style.StaticSeekBarTitle);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setText(getContext().getString(R.string.default_title));
        status = new TextView(context);
        status.setTextAppearance(R.style.DynamicSeekBarTitle);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setText(getContext().getString(R.string.default_status));
        addView(title);
        addView(param);
        addView(status);
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
        Drawable background = ContextCompat.getDrawable(context,R.drawable.seekbar_border);
        setBackground(background);
        param.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String quantity = String.format(Locale.getDefault(),"%.2f",param.getProgress()*scale);
            if (type != null) {
                String unitQuantity = quantity + type;
                status.setText(unitQuantity);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    });
}
    public void setParam(String _title, int max,int progress)
    {
        title.setText(_title);
        param.setMax(max);
        param.setProgress(progress);
    }
    public int getProgress()
    {
        return param.getProgress();
    }

    @Override
    protected void onMeasure(int width, int height) {
        int wrap = LayoutParams.WRAP_CONTENT;
        setMeasuredDimension(wrap, wrap);
        super.onMeasure(wrap, wrap);
    }
}
