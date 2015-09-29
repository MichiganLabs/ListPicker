package com.michiganlabs.listpicker;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private Context context;
    private boolean checked = false;
    private int selectionTextColor;

    public CheckableLinearLayout(Context context) {
        this(context, null);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        selectionTextColor = ContextCompat.getColor(context, android.R.color.white);
    }

    public void setSelectionTextColor(int color) {
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
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
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
