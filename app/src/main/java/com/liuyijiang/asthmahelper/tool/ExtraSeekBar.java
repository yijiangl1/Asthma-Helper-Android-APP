package com.liuyijiang.asthmahelper.tool;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatSeekBar;
import com.liuyijiang.asthmahelper.R;

public class ExtraSeekBar extends AppCompatSeekBar {
    private int startPoint;
    private int numTagId;
    private int slideBarId;

    public ExtraSeekBar(Context context) {
        super(context);
    }

    public ExtraSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtraSeekBar);
        startPoint = a.getInteger(R.styleable.ExtraSeekBar_start_point, 0);
        numTagId = a.getResourceId(R.styleable.ExtraSeekBar_num_tag_id, 0);
        slideBarId = a.getResourceId(R.styleable.ExtraSeekBar_slide_bar_id, 0);
        a.recycle();
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getNumTagId() {
        return numTagId;
    }

    public int getSlideBarId() {
        return slideBarId;
    }
}
