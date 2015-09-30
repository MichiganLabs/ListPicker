package com.michiganlabs.listpicker;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private boolean checked = false;
    private int selectionTextColor;

    public CheckableLinearLayout(Context context) {
        this(context, null);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //noinspection deprecation
        selectionTextColor = getResources().getColor(android.R.color.white);
    }

    public void setSelectionTextColor(@ColorInt int color) {
        selectionTextColor = color;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        TextView textView = (TextView) findViewById(R.id.item_text);
        if (textView != null) {
            if (checked) {
                textView.setTextColor(selectionTextColor);
            } else {
                //noinspection deprecation
                textView.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }
}
