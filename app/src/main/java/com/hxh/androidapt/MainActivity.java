package com.hxh.androidapt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hxh.annotation.Test;

@Test
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
