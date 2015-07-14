package com.michiganlabs.listpicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Arrays;


public class ListPicker extends RelativeLayout implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    public interface OnItemSelectedListener {
        void onItemSelected(int index);
    }

    private ListView listView = null;
    private ListPickerListAdapter adapter = null;
    private Context context;

    private boolean setListView = false;
    private int itemsToShow = 3;
    private int listStart = 0;
    private int listEnd = 0;
    private int paddingItems = 0;
    private int cellHeight;
    private int firstVisibleItem = 0;
    private int selectionForeground = getResources().getColor(android.R.color.white);
    private int selectionBackground = getResources().getColor(android.R.color.darker_gray);

    private OnItemSelectedListener listener = null;

    public ListPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ListPicker
        );

        CharSequence[] arr = getResources().getStringArray(
            a.getResourceId(R.styleable.ListPicker_array, 0)
        );
        ArrayList<CharSequence> items = new ArrayList<>(Arrays.asList(arr));
        setItems(items);

        itemsToShow = a.getInt(R.styleable.ListPicker_itemsToShow, -1);
        if (itemsToShow < 3) {
            // 3 is the minimum
            itemsToShow = 3;
        } else {
            // Round down to odd number
            itemsToShow += ((itemsToShow % 2) - 1);
        }

        // Find indicies of start/end of actual list content
        paddingItems = itemsToShow / 2;
        listStart = paddingItems;
        listEnd = adapter.getCount() - listStart - 1;

        selectionBackground = a.getColor(
            R.styleable.ListPicker_selectionBackground,
            android.R.color.darker_gray
        );
        selectionForeground = a.getColor(
            R.styleable.ListPicker_selectionForeground,
            android.R.color.white
        );
        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.picker_view, this, true);

        listView = (ListView) findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < listStart) {
            position = listStart;
        } else if (position > listEnd) {
            position = listEnd;
        }

        setSelectedIndex(position);
        listView.setItemChecked(position, true);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        onItemClick(parent, view, position, id);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        // we set the listView here, because we need to calculate the cells size, only
        // after listView already has height
        if (!setListView && adapter != null) {
            setListView = true;

            int height = listView.getHeight();
            cellHeight = height / itemsToShow;

            listView.setFadingEdgeLength(cellHeight);
            listView.setAdapter(adapter);

            // Default item selected is middle
            setSelectedIndex(adapter.getCount() / 2);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        View child = view.getChildAt(0);    // first visible child
                        if (child != null) {
                            int firstItem = listView.getFirstVisiblePosition();
                            // set this initially, as required by the docs
                            Rect r = new Rect(0, 0, child.getWidth(), child.getHeight());
                            double height = child.getHeight() * 1.0;

                            view.getChildVisibleRect(child, r, null);
                            if (Math.abs(r.height()) < (int) height / 2) {
                                // show next child
                                firstItem++;
                            }
                            setSelectedIndex(firstItem + paddingItems);
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisible, int visibleItemCount, int totalItemCount) { }
            });
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setItems(ArrayList<CharSequence> items) {
        adapter = new ListPickerListAdapter(context, R.layout.list_item, items);
    }

    public CharSequence getSelected() {
        if (adapter == null) {
            return "";
        }  {
            return adapter.getItem(firstVisibleItem + paddingItems);
        }
    }

    public int getSelectedIndex() {
        if (adapter == null) {
            return 0;
        } else {
            return firstVisibleItem + paddingItems;
        }
    }

    @TargetApi(11)
    public void setSelectedIndex(int index) {
        if (index >= 0 || index < adapter.getCount()) {
            firstVisibleItem = index;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                listView.setSelection(firstVisibleItem - paddingItems);
            } else {
                listView.smoothScrollToPositionFromTop(firstVisibleItem - paddingItems, -1, 200);
            }

            if (listener != null) {
                listener.onItemSelected(getSelectedIndex());
            }
        }
    }

    private class ListPickerListAdapter extends ArrayAdapter<CharSequence> {

        private class ViewHolder {
            TextView text;
        }

        private ArrayList<CharSequence> items;

        public ListPickerListAdapter(Context context, int textViewResourceId, ArrayList<CharSequence> items) {
            super(context, textViewResourceId, items);
            this.items = items;

            // Pad start/end of list with empty items so that list is scrollable to have the first/last item be in the
            // middle of the scrollview.
            for (int i = 0; i < paddingItems; i++) {
                this.items.add("");
                this.items.add(0, "");
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.list_item, null);

                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) convertView.findViewById(R.id.item_text);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (convertView instanceof CheckableLinearLayout) {
                CheckableLinearLayout cv = (CheckableLinearLayout) convertView;
                cv.setSelectionBackgroundColor(selectionBackground);
                cv.setSelectionTextColor(selectionForeground);
            }

            if (viewHolder != null) {
                viewHolder.text.setText("" + items.get(position));

                // Resize cell to
                if (viewHolder.text.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) viewHolder.text.getLayoutParams();
                    p.height = cellHeight;
                    viewHolder.text.setLayoutParams(p);
                    viewHolder.text.requestLayout();
                }
            }

            // Make empty padding items non-selectable
            if (position < listStart) {
                convertView.setClickable(true);
            } else if (position > listEnd) {
                convertView.setClickable(true);
            } else {
                convertView.setClickable(false);
            }

            return convertView;
        }
    }
}
