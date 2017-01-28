package com.rajan.apps.modulicalculator;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Help_Doc supports the Help Documentation activity by loading an .html with specific content.
 *
 * @author  Rajan Aggarwal
 * @version 1.0
 * @since   2017-01-23
 *
 * Copyright 2017, Rajan Aggarwal, All rights reserved.
 */

public class Help_Doc extends AppCompatActivity{


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_doc);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.help_doc_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Help");
        myToolbar.setTitleTextColor(Color.WHITE);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        WebView wv = (WebView) findViewById(R.id.webView1);
        WebSettings ws=wv.getSettings();
        ws.setJavaScriptEnabled(true);
        wv.setBackgroundColor(Color.TRANSPARENT);

        wv.loadUrl("file:///android_asset/helpDoc.html");


    }




    //responds to the "up"/"back" button press
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
