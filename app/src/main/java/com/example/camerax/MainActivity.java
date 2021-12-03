package com.example.camerax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListenableFuture<ProcessCameraProvider> cameraFuture;
    PreviewView previewView;
    ImageButton bTakePicture, imageFlip, bFlash;
    private ImageCapture imageCapture;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
    ProcessCameraProvider processCameraProvider;
    Camera cam;
    private boolean flashActivate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        bTakePicture = findViewById(R.id.bCapture);
        imageFlip = findViewById(R.id.bFlip);
        bFlash = findViewById(R.id.bFlash);

        bTakePicture.setOnClickListener(this);
        imageFlip.setOnClickListener(this);
        bFlash.setOnClickListener(this);
        isStoragePermissionGranted();
        startCamera();
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                return true;
            } else {

                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bTakePicture.setEnabled(true);
    }

    private Executor getExecutor(){
        return ContextCompat.getMainExecutor(this);
    }

    private void startCamera() {
        cameraFuture = ProcessCameraProvider.getInstance(this);
        cameraFuture.addListener(() -> {
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            try {
                processCameraProvider = cameraFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                processCameraProvider.unbindAll();

                // lensFacing is used here
                processCameraProvider.bindToLifecycle((LifecycleOwner) this, lensFacing, imageCapture, preview);
                cam = processCameraProvider.bindToLifecycle((LifecycleOwner)this, lensFacing, imageCapture, preview);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    private void capturePhoto(){
        turnOnFlash();
        bTakePicture.setEnabled(false);
        //File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/CameraXPhotos";
        //File photoDir = new File("/mnt/sdcard/Pictures/CameraXPhotos");
        //File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/CameraXPhotos");
        File photoDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/CameraXPhotos");

        if(!photoDir.exists()){
            photoDir.mkdir();
        }

        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";

        File photoFile = new File(photoFilePath);
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback(){

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(getApplicationContext(), "Foto fuardada exitosamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), com.example.camerax.Preview.class);
                        intent.putExtra("path", photoFile);
                        startActivityForResult(intent, 0);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        bTakePicture.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Error guardando la foto " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check that it is the SecondActivity with an OK result
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                try{
                    File imgFile = (File) data.getExtras().get("filePath");
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    public void turnOnFlash(){
        if (flashActivate) {
            cam.getCameraControl().enableTorch(true);
        }
    }

    public void turnOnOffFlash(){
        if ( cam.getCameraInfo().hasFlashUnit() && !flashActivate ) {
            bFlash.setImageResource(android.R.color.transparent);
            bFlash.setBackgroundResource(R.drawable.ic_baseline_flash_on_24);
            flashActivate = true;
        } else {
            bFlash.setImageResource(android.R.color.transparent);
            bFlash.setBackgroundResource(R.drawable.ic_baseline_flash_off_24);
            flashActivate = false;
        }
    }

    private void flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
        startCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bCapture:
                capturePhoto();
                break;

            case R.id.bFlip:
                flipCamera();
                break;

            case R.id.bFlash:
                turnOnOffFlash();
                break;

        }
    }
}