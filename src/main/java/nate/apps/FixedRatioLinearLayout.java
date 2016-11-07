package nate.apps;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import nate.apps.fragmap.R;

/**
 * Created by nathan on 29/10/2016.
 * Linear layout with fixed aspect ratio
 * used to ensure signpost always displays correctly
 */

public class FixedRatioLinearLayout extends LinearLayout {
    private double aspect_ratio;
    public FixedRatioLinearLayout(Context context) {
        super(context);
    }

    public FixedRatioLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.FixedRatioLinearLayout,0, 0);
        try {
            //get aspect ratio of view make layout square by default if there is no value set
            aspect_ratio = a.getFloat(R.styleable.FixedRatioLinearLayout_ratio,1f);
        } finally {
            a.recycle();
        }
    }

    public FixedRatioLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //measure view and fix width and height to fit in while using asigned aspect
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w > (int)((aspect_ratio * h) + 0.5)) {
            w = (int)((aspect_ratio * h) + 0.5);
        } else {
            h = (int)((w / aspect_ratio) + 0.5);
        }
        super.onMeasure( MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }
}
