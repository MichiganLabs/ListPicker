package com.michiganlabs.listpickerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.michiganlabs.listpicker.ListPicker;

import java.util.ArrayList;


public class MainActivity extends Activity implements ListPicker.OnItemSelectedListener {
    ArrayList<String> items = new ArrayList<>();
    ListPicker<String> picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 50; i++) {
            items.add("" + i);
        }

        picker = (ListPicker) findViewById(R.id.picker);
        picker.setItems(items);
        picker.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(int index) {
        Log.i("MainActivity", "Selected item: " + picker.getItemAtIndex(index) + " at index: " + index);
    }
}
