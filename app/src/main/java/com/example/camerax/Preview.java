package com.example.camerax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Preview extends Activity implements View.OnClickListener {
    File imgFile2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ImageView image = (ImageView) findViewById(R.id.imageView);
        ImageButton cancel = findViewById(R.id.cancel);
        ImageButton okay = findViewById(R.id.okay);

        cancel.setOnClickListener(this);
        okay.setOnClickListener(this);

        imgFile2 = (File) getIntent().getExtras().get("path");
        image.setImageBitmap(BitmapFactory.decodeFile(imgFile2.getAbsolutePath()));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                File file = new File(imgFile2.getAbsolutePath());
                if(file.delete()){
                    finish();
                }
                break;

            case R.id.okay:
                Intent intent = new Intent();
                intent.putExtra("filePath", imgFile2);
                setResult(RESULT_OK, intent);
                finish();
                break;

        }
    }
}
