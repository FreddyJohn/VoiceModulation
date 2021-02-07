package com.example.voicemodulation.controls;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.voicemodulation.R;


public class Controller extends LinearLayout {
    private final TextView status;
    private final TextView title;
    private final ControlBar param;
    public Controller(Context context, @Nullable AttributeSet attrs,String type,double scale) {
        super(context, attrs);
        param = new ControlBar(context,null);
        title = new TextView(context);
        title.setTextAppearance(R.style.StaticSeekBarTitle);
        title.setText("title");
        status = new TextView(context);
        status.setTextAppearance(R.style.DynamicSeekBarTitle);
        status.setText("status");
        addView(title);
        addView(param);
        addView(status);
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
        Drawable b = context.getDrawable(R.drawable.seekbar_border);
        setBackground(b);
        param.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String quantity =String.format("%.2f",param.getProgress()*scale);
            if (type!=null) { status.setText(quantity+type); }
            else { status.setText(quantity); }
            //status.setText(param.getProgress());
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
