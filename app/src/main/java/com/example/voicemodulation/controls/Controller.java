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
            if (param.getProgress()!=0) {
                String quantity =String.format("%.2f",param.getProgress()*scale);
                //TODO all record controls have types associated with their seek bar controller so this conditional is usuless
                if (type != null) {
                    status.setText(quantity + type);
                }
            }
            if (!zeroCase) {
                status.setText(quantityToType.quanToType(param.getProgress()));
                //status.setText(quantity);
            }
            //TODO if less than scale for when we eventually implement fine and more sensitive scroll
            //  based on user touch feedback
            // if controller uses 0
            /*
            if (param.getProgress()==0)
            {
                String quantity =String.format("%.2f",scale);
                if (type != null) {
                    status.setText(scale + type);
                } else {
                    status.setText(quantity);
                }
            }

             */
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
    public void setSeekBarEnabled(boolean enabled)
    {
        //param.setActivated(false);
    }
    public void setTypeSwitch(ControlCases.seekers runnable){
        this.quantityToType = runnable;
        //(variable) -> switch statement
    }
    public void setZeroCase(Boolean _zeroCase){
        this.zeroCase = _zeroCase;
    }

    @Override
    protected void onMeasure(int width, int height) {
        int wrap = LayoutParams.WRAP_CONTENT;
        setMeasuredDimension(wrap, wrap);
        super.onMeasure(wrap, wrap);
    }
}
