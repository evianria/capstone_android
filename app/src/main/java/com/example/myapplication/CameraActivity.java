package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

public class CameraActivity extends AppCompatActivity {

    private  static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilepath;
    private Uri photoUri;
    String UPLOAD_URL = "";
    String profileImgPath;
    ImageView faceImg;
    private AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // 권한 체크
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .check();
        //얼굴카메라 작동
        findViewById(R.id.faceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                       photoFile = createImageFile();
                    } catch (IOException e){

                    }

                    if(photoFile != null) {
                        photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE); // 다음 intent로 화면 전환이 일어났을때 값을 가져 와주는 역할

                    }
                }
            }
        });



        // 완료버튼 누를 시 다음페이지로 이동
        Button button =(Button)findViewById(R.id.finishButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEmpty(profileImgPath)) {
                    displayAlert("사진을 선택하세요");
                    return;

                }

                Ion.with(CameraActivity.this)
                        .load(UPLOAD_URL)
                        .setMultipartFile("image", "image/jpeg", new File(profileImgPath))
                        .asJsonObject()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<JsonObject>>() {
                            @Override
                            public void onCompleted(Exception e, Response<JsonObject> result) {
                                if(e != null){
                                    Toast.makeText(CameraActivity.this, "Error is: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }else{
                                    switch (result.getHeaders().code()) {
                                        case 500:
                                            Toast.makeText(CameraActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 200:
                                            Toast.makeText(CameraActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                                            Bitmap bitmap = BitmapFactory.decodeResource(CameraActivity.this.getResources(),
                                                    R.drawable.imageholder);
                                            faceImg.setImageBitmap(bitmap);
                                            profileImgPath = null;
                                            break;
                                    }
                                }
                            }
                        });

            }
        });
    }


    // 사진 중복을 없애는 것
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, ".jpg",storageDir
        );
        imageFilepath = image.getAbsolutePath();
        return image;
    }

    // 얼굴사진
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilepath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilepath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if(exif != null){
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            }else {
                exifDegree =0;
            }
            ((ImageView)findViewById(R.id.faceImage)).setImageBitmap(rotate(bitmap,exifDegree));

        }
    }
    // 지문사진


    // 이미지 회전
    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        //허용했을 때
        public void onPermissionGranted() {
            Toast.makeText(getApplicationContext(), "권한이 허용됨", Toast.LENGTH_SHORT).show();
        }

        @Override
        //거절했을 때
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한이 거부됨", Toast.LENGTH_SHORT).show();
        }
    };

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
