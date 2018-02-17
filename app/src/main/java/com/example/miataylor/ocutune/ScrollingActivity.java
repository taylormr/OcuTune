package com.example.miataylor.ocutune;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
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
import java.io.IOException;

public class ScrollingActivity extends AppCompatActivity {




    private ImageView imageHolder;
    private final int requestCode = 20;
    private final int PICK_IMAGE = 1;
    private String imagePath;

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
            startActivityForResult(Intent.createChooser(takePictureIntent,"Select Picture"), requestCode);
        }
    }
    public void openGallery(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //For open camera
        super.onActivityResult(requestCode, resultCode, data);
        if((this.requestCode == requestCode) && resultCode == RESULT_OK){
            Bitmap bitmapCamera = (Bitmap)data.getExtras().get("data");
            bitmapCamera = getRoundedBitmap(bitmapCamera);
            imageHolder.setImageBitmap(bitmapCamera);

        }

        //For open gallery
        if (requestCode == PICK_IMAGE) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmapGallery = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                bitmapGallery = getRoundedBitmap(bitmapGallery);
                imageHolder.setImageBitmap(bitmapGallery);
            }
            catch (IOException e){
                System.out.print("Err");
            }
        }

    }

    public Bitmap getRoundedBitmap(Bitmap bitmap){
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth(), paint);
        return circleBitmap;
    }




}
