package com.rajan.apps.modulicalculator;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Welcome_Screen sets up the home page of the app, including buttons for app navigation.
 *
 * @author  Rajan Aggarwal
 * @version 1.0
 * @since   2017-01-23
 *
 * Copyright 2017, Rajan Aggarwal, All rights reserved.
 */

public class Welcome_Screen extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.welcome_screen_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Home");
        myToolbar.setTitleTextColor(Color.WHITE);
    }

    public void openCalculator(View view){
        Intent intent = new Intent(this, Calculator.class);
        startActivity(intent);
    }

    public void openHelp_Doc(View view){
        Intent intent = new Intent(this, Help_Doc.class);
        startActivity(intent);
    }

}
