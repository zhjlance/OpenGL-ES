package com.example.glsurfaceviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceViewTest mGlSurfaceViewTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlSurfaceViewTest = new GLSurfaceViewTest(this);
        setContentView(mGlSurfaceViewTest);
    }
}