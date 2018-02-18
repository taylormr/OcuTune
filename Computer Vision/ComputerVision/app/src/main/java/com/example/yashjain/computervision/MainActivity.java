package com.example.yashjain.computervision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;
import android.database.Cursor;

import java.io.File;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.Tag;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int IMAGE_REQUEST_CODE = 1;

    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("b128909fed35443f9c1831cc16793582", "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");
    ImageView imageView;
    ImageButton btnprocess;
    Bitmap mBitmap;
    final int requestCode = 20;
    final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton selectButton = (ImageButton) findViewById(R.id.btnSelect);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton cameraButton = (ImageButton) findViewById(R.id.btnCamera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(takePictureIntent,"Select Picture"), requestCode);
                }
            }
        });


        // This is where we get the image from the user once they have pressed the button.
        imageView = (ImageView) findViewById(R.id.imageView);
        btnprocess = (ImageButton) findViewById(R.id.btnProcess);
        btnprocess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mBitmap != null) {
                    processImage(mBitmap);
                }
            }
        });
    }

    private void processImage(Bitmap mBitmap) {
        //Convert image into stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        @SuppressLint("StaticFieldLeak")
        final AsyncTask<InputStream, String, AnalysisResult> visionTask = new AsyncTask<InputStream, String, AnalysisResult>() {
            ProgressDialog mDialog;

            @Override
            protected AnalysisResult doInBackground(InputStream... params) {
                try {
                    publishProgress("Recognizing...");
                    String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories", "Description", "Tags"};
                    String[] details = {};

                    return visionServiceClient.analyzeImage(params[0], features, details);
                } catch (Exception e) {
                    Log.e("MainActivity", e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.show();
            }

            @Override
            protected void onPostExecute(AnalysisResult analysisResult) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }

                if (analysisResult != null) {
                    TextView textView = (TextView) findViewById(R.id.spotify);
                    StringBuilder stringbuilder = new StringBuilder();
                    if (analysisResult.tags != null) {
                        for (Tag tag : analysisResult.tags) {
                            stringbuilder.append(tag.name + ", ");
                        }

                        textView.setText(stringbuilder);

                    } else {
                        textView.setText("No Tags available");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong. That's all I know...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mDialog.setMessage(values[0]);
            }
        };
        visionTask.execute(inputStream);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Yash
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    Uri imageUri = data.getData();
                    Log.d("MainActivity", imageUri.toString());
                    imageView.setImageURI(imageUri);
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    } catch (IOException e) {
                        Log.e(TAG, "Content resolver error!", e);
                    }

                    break;
            }
        } else {
            // An Error
        }
        //Mia
        //For open camera
        if((this.requestCode == requestCode) && resultCode == RESULT_OK){
            Bitmap bitmapCamera = (Bitmap)data.getExtras().get("data");
            //bitmapCamera = getRoundedBitmap(bitmapCamera);
            imageView.setImageBitmap(bitmapCamera);

        }

        //For open gallery
        if (requestCode == PICK_IMAGE) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmapGallery = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //bitmapGallery = getRoundedBitmap(bitmapGallery);
                imageView.setImageBitmap(bitmapGallery);
            }
            catch (IOException e){
                System.out.print("Err");
            }
        }
    }
}
