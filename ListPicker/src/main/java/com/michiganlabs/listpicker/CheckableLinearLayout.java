package com.michiganlabs.listpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.michiganlabs.listpicker.R;

/**
 * Created by josh on 7/14/15.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private boolean checked = false;
    private int selectionBackgroundColor = getResources().getColor(android.R.color.transparent);
    private int selectionTextColor = getResources().getColor(android.R.color.white);

    public CheckableLinearLayout(Context context) {
        super(context);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSelectionBackgroundColor(int color) {
        selectionBackgroundColor = color;
    }

    public void setSelectionTextColor(int color) {
        selectionTextColor = color;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        TextView textView = (TextView) findViewById(R.id.item_text);
        if (this.checked) {
            setBackgroundColor(selectionBackgroundColor);
            if (textView != null) {
                textView.setTextColor(selectionTextColor);
            }
        } else {
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
            if (textView != null) {
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
