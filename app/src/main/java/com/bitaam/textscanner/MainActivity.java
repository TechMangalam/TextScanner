package com.bitaam.textscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.*;

public class MainActivity extends AppCompatActivity {

    TextView scannedText;
    Button scanBtn,takeImg;
    ImageView capturedImg;
    Bitmap photo=null;
    InterstitialAd interstitialAd;
    AdRequest adRequest;
    String imageTextR = "";

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanBtn = findViewById(R.id.scanBtn);
        takeImg = findViewById(R.id.captureBtn);
        scannedText = findViewById(R.id.textScanned);
        capturedImg = findViewById(R.id.imgScanned);
        scannedText.setMovementMethod(new ScrollingMovementMethod());

        AdView homeBan = findViewById(R.id.homeBan);
        MobileAds.initialize(getApplicationContext());
        homeBan.loadAd(new AdRequest.Builder().build());
        adRequest = new AdRequest.Builder().build();

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-8103108161786269/1888287818");
        interstitialAd.loadAd(adRequest);

        onClickEvents();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.share)
        {
            if (imageTextR.length() > 1){
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);

                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share text");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, imageTextR);

                startActivity(Intent.createChooser(intent, "Select app to share"));
            }else{
                Toast.makeText(this, "No text found ,Insert image and scan image", Toast.LENGTH_SHORT).show();
            }

        }else if (item.getItemId() == R.id.shareApp) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share app");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "https://drive.google.com/file/d/1y2Q0wkAsYSexgOFjc2f5LE1PDjO7XGRg/view?usp=sharing \nOR\n  https://bitaam.online/EngTextScanner.html");

            startActivity(Intent.createChooser(intent, "Select app to share"));
        }

        return true;
    }

    private void onClickEvents() {

        takeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                scannedText.setVisibility(View.GONE);
                startActivityForResult(intent,1000);
                //scanText(photo);

            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (photo == null){
                    Toast.makeText(MainActivity.this, "First insert image to scan", Toast.LENGTH_SHORT).show();
                }else{
                    if (interstitialAd.isLoaded())
                        interstitialAd.show();

                    scanText(photo);
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){

                Uri imgUri = data.getData();
                Bitmap bmp = null;
                try{
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
                    photo = bmp;
                }catch (Exception e){}
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG,25,baos);
                byte[] fileInBytes = baos.toByteArray();
                capturedImg.setVisibility(View.VISIBLE);

                capturedImg.setImageURI(imgUri);


            }
        }

    }

    private void scanText(Bitmap bitmap){

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        Frame imageFrame = new Frame.Builder()

                .setBitmap(bitmap)                 // your image bitmap
                .build();

        String imageText = "";
        imageTextR = "";


        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            imageText = textBlock.getValue();
            imageTextR = imageTextR+" \n"+textBlock.getValue();
            // return string
        }

        if (imageTextR.length() > 1){
            scannedText.setVisibility(View.VISIBLE);
            capturedImg.setVisibility(View.GONE);
            scannedText.setText(imageTextR);
        }else{
            Toast.makeText(this, "Check text in image is in right orientation or image does not have text", Toast.LENGTH_SHORT).show();
        }



    }


}