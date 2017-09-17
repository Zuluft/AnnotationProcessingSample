package com.example.giodz.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.annotationsample.Navigator;
import com.example.lib.TestAnnotation;


@TestAnnotation
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Navigator.startMainActivity(this);
    }
}
