package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.io.IOException;

public class ImageUploadActivity extends AppCompatActivity {

    ImageView imageToUpload;
    private static final int IMG_REQUEST = 1;
    Button uploadBtn;
    String profileImgPath;
    Bitmap bitmap;
    private AlertDialog.Builder builder;
    String UPLOAD_URL = "http://a6091e864263.ngrok.io";

    @Override
    protected void onStart() {
        getPermissions();
        super.onStart();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageupload);

        initVars();

        imageToUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMG_REQUEST);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEmpty(profileImgPath)) {
                    displayAlert("사진을 선택하세요");
                    return;

                }

                Ion.with(ImageUploadActivity.this)
                        .load(UPLOAD_URL)
                        .setMultipartFile("image", "image/jpeg", new File(profileImgPath))
                        .asJsonObject()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<JsonObject>>() {
                            @Override
                            public void onCompleted(Exception e, Response<JsonObject> result) {
                                if(e != null){
                                    Toast.makeText(ImageUploadActivity.this, "Error is: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }else{
                                    switch (result.getHeaders().code()) {
                                        case 500:
                                            Toast.makeText(ImageUploadActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 200:
                                            Toast.makeText(ImageUploadActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                                            Bitmap bitmap = BitmapFactory.decodeResource(ImageUploadActivity.this.getResources(),
                                                    R.drawable.imageholder);
                                            imageToUpload.setImageBitmap(bitmap);
                                            profileImgPath = null;
                                            break;
                                    }
                                }
                            }
                        });
            }
        });


    }

    private void initVars() {
        builder = new AlertDialog.Builder(this);
        imageToUpload = findViewById(R.id.imagetoupload);
        uploadBtn = findViewById(R.id.uploadbtn);
    }

    private void getPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null) {
            if(requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null){
                Uri path = data.getData();
                if(path != null){
                    profileImgPath = FetchPath.getPath(this, path);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                        imageToUpload.setImageBitmap(getCroppedBitmap(bitmap));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }


    public static boolean isEmpty(String field){

        return field == null || field.isEmpty();
    }
    public void displayAlert(String message) {
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
