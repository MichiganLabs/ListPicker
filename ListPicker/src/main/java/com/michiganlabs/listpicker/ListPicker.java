package com.michiganlabs.listpicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;


public class ListPicker<T> extends RelativeLayout implements AdapterView.OnItemClickListener,
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

        if (isInEditMode()) {
            return;
        }

        TypedArray a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ListPicker
        );

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

        selectionBackground = a.getColor(
            R.styleable.ListPicker_selectionBackground,
            android.R.color.darker_gray
        );
        selectionForeground = a.getColor(
            R.styleable.ListPicker_selectionForeground,
            android.R.color.white
        );
        a.recycle();

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.picker_view, this, true);

        listView = (ListView) findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setSelectedIndex(position - paddingItems);

        if (listener != null) {
            listener.onItemSelected(getSelectedIndex());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        onItemClick(parent, view, position, id);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (!setListView && adapter != null) {
            setListView = true;

            int height = listView.getHeight();
            cellHeight = height / itemsToShow;

            listView.setFadingEdgeLength(cellHeight);
            listView.setAdapter(adapter);

            listView.setOnScrollListener(new SnappingListener());
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setItems(List<T> items) {
        adapter = new ListPickerListAdapter(context, R.layout.list_item, new ArrayList<>(items));
        listView.setAdapter(adapter);
        listEnd = adapter.getCount() - listStart - 1;
        setSelectedIndex(0);
    }

    public T getSelected() {
        if (adapter == null) {
            return null;
        }  {
            return getItemAtIndex(getSelectedIndex());
        }
    }

    public int getSelectedIndex() {
        if (adapter == null) {
            return -1;
        } else {
            return firstVisibleItem - paddingItems;
        }
    }

    public T getItemAtIndex(int index) {
        if (adapter != null) {
            index += listStart;
            if (index >= listStart && index <= listEnd) {
                return adapter.getItem(index);
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }
        return null;
    }

    public void setSelectedIndex(int index) {
        index += listStart;
        if (index >= listStart && index <= listEnd) {
            firstVisibleItem = index;
            scrollListViewToPositionFromTop(listView, firstVisibleItem - paddingItems, 150);
            listView.setItemChecked(index, true);
        }
    }

    @TargetApi(11)
    private void scrollListViewToPositionFromTop(AbsListView view, int position, int duration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            view.setSelection(position);
        } else {
            view.smoothScrollToPositionFromTop(position, 0, duration);
        }
    }

    private class ListPickerListAdapter extends ArrayAdapter<T> {

        private class ViewHolder {
            TextView text;
        }

        private class EmptyItem {
            @Override
            public String toString() {
                return "";
            }
        }

        private ArrayList<T> items;

        public ListPickerListAdapter(Context context, int textViewResourceId, ArrayList<T> items) {
            super(context, textViewResourceId, items);
            this.items = items;

            // Pad start/end of list with empty items so that list is scrollable to have the first/last item be in the
            // middle of the scrollview.
            for (int i = 0; i < paddingItems; i++) {
                this.items.add((T) new EmptyItem());
                this.items.add(0, (T) new EmptyItem());
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public T getItem(int position) {
            return items.get(position);
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
                viewHolder.text.setText(items.get(position).toString());

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

    private class SnappingListener implements AbsListView.OnScrollListener {
        boolean scrolling = false;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (scrolling){
                        scrolling = false;

                        // get first visible item
                        View itemView = view.getChildAt(0);
                        int top = Math.abs(itemView.getTop()); // top is a negative value
                        int bottom = Math.abs(itemView.getBottom());
                        if (top >= bottom){
                            scrollListViewToPositionFromTop(view, view.getFirstVisiblePosition() + 1, 150);
                        } else {
                            scrollListViewToPositionFromTop(view, view.getFirstVisiblePosition(), 150);
                        }
                    }
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    scrolling = true;
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
    }
}
