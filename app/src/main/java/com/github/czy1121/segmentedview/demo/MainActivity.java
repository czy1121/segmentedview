package com.github.czy1121.segmentedview.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.github.czy1121.view.SegmentedView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SegmentedView.OnItemSelectedListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        bind(R.id.sv1);
        bind(R.id.sv2);
        bind(R.id.sv3);
        bind(R.id.sv4);
        bind(R.id.sv5);
        bind(R.id.sv6);
    }

    void bind(int resId) {
        ((SegmentedView)findViewById(resId)).setOnItemSelectedListener(this);
    }

    @Override
    public void onSelected(int index, String text) {
        Toast.makeText(this,  index + " : " + text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
    }
}
