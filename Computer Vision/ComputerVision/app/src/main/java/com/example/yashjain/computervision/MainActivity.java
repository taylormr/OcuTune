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
import android.os.StrictMode;
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
import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback{

    private static final String CLIENT_ID = "9d91108e04bb42f488dc10c2778367e8";
    private static final String REDIRECT_URI = "computer-vision://callback";
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    public String token;
    private static final String TAG = "MainActivity";
    public static final int TRACK_LIMIT = 3;

    private static final int IMAGE_REQUEST_CODE = 1;
    public final String pre_html = "<div>";
    public String html_end = "</div>";
    public final String I_START = "<iframe src=\"https://open.spotify.com/embed/track/";
    public final String I_END = "\"\nwidth=\"100%\" height=\"380\" frameborder=\"0\" allowtransparency=\"true\"></iframe>";
    public String[] samples = {"45pvLeeK3mcKz1O8hPmYMX", "45pvLeeK3mcKz1O8hPmYMX", "45pvLeeK3mcKz1O8hPmYMX", "45pvLeeK3mcKz1O8hPmYMX", "45pvLeeK3mcKz1O8hPmYMX", "45pvLeeK3mcKz1O8hPmYMX"};

    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("b128909fed35443f9c1831cc16793582", "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");
    ImageView imageView;
    ImageButton btnprocess;
    Bitmap mBitmap;
    public TextView textView;
    public WebView mSpotifyEmbed;
    final int requestCode = 20;
    final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpotifyEmbed = findViewById(R.id.spotify_embed);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        mSpotifyEmbed.getSettings().setJavaScriptEnabled(true);




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
        textView = (TextView) findViewById(R.id.pretext);
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
                    Log.d("MainActivity_attempt", "Before api call");
                    return visionServiceClient.analyzeImage(params[0], features, details);

                } catch (Exception e) {
                    Log.e("MainActivity_error", e.getMessage(), e);
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

                //Naman

                /*SpotifyApi api = new SpotifyApi();
                api.setAccessToken(token);

                SpotifyService spotify = api.getService();

                Log.d("BEFORE_TRACK", "HELLO WORLD");
                TracksPager tracks = spotify.searchTracks("beach");
                TracksPager tracks2 = spotify.searchTracks("indoor");
                TracksPager tracks3 = spotify.searchTracks("person");

                Toast.makeText(MainActivity.this, "TRACKS FOUND!!!", Toast.LENGTH_SHORT).show();
                Log.d("AFTER_TRACK", tracks.tracks.items.get(0).name);
                html += I_START + tracks.tracks.items.get(0).id + I_END;
                Log.d("AFTER_TRACK", tracks2.tracks.items.get(0).name);
                html += I_START + tracks2.tracks.items.get(0).id + I_END;
                Log.d("AFTER_TRACK", tracks3.tracks.items.get(0).name);
                html += I_START + tracks3.tracks.items.get(0).id + I_END;
                html += html_end;

                textView.setText("");
                mSpotifyEmbed.loadData(html, "text/html", null);*/

                //end Naman

                if (analysisResult != null) {

                    //Naman

                    ArrayList<Tag> clone = new ArrayList<>();
                    StringBuilder stringbuilder = new StringBuilder();
                    if (analysisResult.tags != null) {
                        for (Tag tag : analysisResult.tags) {
                            if(!tag.name.equals("indoor")) {
                                stringbuilder.append(tag.name + ", ");
                            }
                            else{
                                clone.add(tag);
                            }
                        }
                        if(clone.size() > 0){
                            analysisResult.tags.remove(clone.get(0));
                            clone.remove(0);
                        }

                        //when there are actual tags
                        if(stringbuilder.length() > 0){

                            SpotifyApi api = new SpotifyApi();
                            api.setAccessToken(token);

                            SpotifyService spotify = api.getService();

                            Log.d("BEFORE_TRACK", "HELLO WORLD");
                            String html = "<div>";
                            if(analysisResult.tags.size() > 0) {
                                TracksPager tracks = spotify.searchTracks(analysisResult.tags.get(0).name);
                                html += I_START + tracks.tracks.items.get(0).id + I_END;
                            }
                            if(analysisResult.tags.size() > 1) {
                                TracksPager tracks2 = spotify.searchTracks(analysisResult.tags.get(1).name);
                                html += I_START + tracks2.tracks.items.get(0).id + I_END;
                            }
                            if(analysisResult.tags.size() > 2) {
                                TracksPager tracks3 = spotify.searchTracks(analysisResult.tags.get(2).name);
                                html += I_START + tracks3.tracks.items.get(0).id + I_END;
                            }
                            Toast.makeText(MainActivity.this, "TRACKS FOUND!!!", Toast.LENGTH_SHORT).show();
                            html += html_end;

                            textView.setText("");
                            mSpotifyEmbed.loadData(html, "text/html", null);
                        }
                        else{
                            String html = "<div>";
                            for(int i = 0; i < 3; i++){
                                int random = (int) (Math.random() * samples.length);
                                html += I_START + samples[random] + I_END;
                            }
                            html += html_end;


                            textView.setText("No tags...");
                            mSpotifyEmbed.loadData(html, "text/html", null);
                        }
                        //end Naman

                        Toast.makeText(MainActivity.this, "Tags: " + stringbuilder, Toast.LENGTH_SHORT).show();


                    } else {
                        textView.setText("No Tags available");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong. That's all I know...", Toast.LENGTH_SHORT).show();

                    //Naman
                    //When Computer Vision fails
                    String html = "<div>";
                    for(int i = 0; i < 3; i++){
                        int random = (int) (Math.random() * samples.length);
                        html += I_START + samples[random] + I_END;
                    }
                    html += html_end;

                    textView.setText("CV failed");
                    mSpotifyEmbed.loadData(html, "text/html", null);
                    //end Naman

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

        //Naman
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                token = response.getAccessToken();
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }


    }

    //Naman's methods
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

        //mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    public void onLoginFailed(int i) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed" + error);
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }
}
