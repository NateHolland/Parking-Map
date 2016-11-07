package nate.apps;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by nathan on 29/10/2016.
 */

public class AutosizeText extends TextView{
    private boolean should_resize;
    private int widthLimit;
    private int heightLimit;
    //maximumn text size
    private float maxtextsize = 52.0f;

    public AutosizeText(Context context) {
        super(context);
    }

    public AutosizeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutosizeText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutosizeText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        //text changed so recalculate size
        //resizeText();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            should_resize = true;
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed || should_resize) {
            widthLimit = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
            heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
            resizeText();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private void resizeText() {
        if(widthLimit>0&&heightLimit>0){
            Paint textpaint = getPaint();
            textpaint.setTextSize(maxtextsize);
            double text_height = Math.abs(textpaint.ascent()) + Math.abs(textpaint.descent());
            double text_width = textpaint.measureText(getText().toString());
            while (text_width>widthLimit||text_height>heightLimit){
                textpaint.measureText(getText().toString());
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textpaint.getTextSize()-1);
                text_width = textpaint.measureText(getText().toString());
                text_height = Math.abs(textpaint.ascent()) + Math.abs(textpaint.descent());
            }
        }
    }
}
