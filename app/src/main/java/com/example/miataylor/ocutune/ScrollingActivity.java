package com.example.miataylor.ocutune;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ScrollingActivity extends AppCompatActivity {




    private ImageView imageHolder;
    private final int requestCode = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        imageHolder = (ImageView)findViewById(R.id.picPreview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openCamera(View view) {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if(this.requestCode == requestCode && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap)data.getExtras().get("data");
            imageHolder.setImageBitmap(bitmap);
        }


    }



}
