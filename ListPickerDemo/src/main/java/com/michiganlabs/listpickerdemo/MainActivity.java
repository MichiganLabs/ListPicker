package com.michiganlabs.listpickerdemo;

import android.app.Activity;
import android.os.Bundle;
import com.michiganlabs.listpicker.ListPicker;

import java.util.ArrayList;


public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<CharSequence> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            items.add("" + i);
        }

        ListPicker picker = (ListPicker) findViewById(R.id.picker);
        picker.setItems(items);
    }
}
