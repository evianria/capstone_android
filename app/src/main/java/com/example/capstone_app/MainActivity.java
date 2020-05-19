package com.example.capstone_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static java.sql.DriverManager.println;

public class MainActivity extends AppCompatActivity {
    EditText editTextURL;
    EditText editTextID;
    EditText editTextPW;
    Button buttonLogin;

    String ID;
    String PW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ID = editTextID.getText().toString();
                PW = editTextPW.getText().toString();
                request();

            }
        });

    }

    public void init(){
        editTextURL= (EditText)findViewById(R.id.editText1);
        editTextID = (EditText)findViewById(R.id.editText2);
        editTextPW = (EditText)findViewById(R.id.editText3);
        buttonLogin = (Button)findViewById(R.id.button);
    }
    public void request(){
        //url 요청주소 넣는 editText를 받아 url만들기
        String url = editTextURL.getText().toString();

        //JSON형식으로 데이터 통신
        JSONObject testjson = new JSONObject();
        try {
            //입력해둔 edittext의 id와 pw값을 받아와 put : 데이터를 json형식으로 변환
            testjson.put("id", editTextID.getText().toString());
            testjson.put("password", editTextPW.getText().toString());
            String jsonString = testjson.toString(); //완성된 json 포맷

            //전송
            final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,testjson, new Response.Listener<JSONObject>() {

                //데이터 전달을 끝내고 응답받기
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        println("데이터전송 성공");

                        //받은 json형식의 응답을 받아
                        JSONObject jsonObject = new JSONObject(response.toString());

                        //key값에 따라 value값을 쪼개서 받아옴
                        String resultId = jsonObject.getString("approve_id");
                        String resultPassword = jsonObject.getString("approve_pw");

                        //만약 값이 같다면 로그인에 성공
                        if(resultId.equals("OK") & resultPassword.equals("OK")){
                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
            //
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}