package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {

    EditText editTextURL;
    EditText editTextID;
    EditText editTextPW;
    Button buttonLogin;

    public static String ID;
    String PW;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

    public void init() {
        editTextURL = (EditText) findViewById(R.id.ip_port);
        editTextID = (EditText) findViewById(R.id.idText);
        editTextPW = (EditText) findViewById(R.id.passwordText);
        buttonLogin = (Button) findViewById(R.id.loginButton);
    }

    public void request() {
        //url 요청주소 넣는 editText를 받아 url만들기
       String url = "http://192.168.43.218:3000/process/ancommute";


        //JSON형식으로 데이터 통신
        JSONObject testjson = new JSONObject();
        try {
            //입력해둔 edittext의 id와 pw값을 받아와 put : 데이터를 json형식으로 변환
            testjson.put("id", editTextID.getText().toString());
            ID = editTextID.getText().toString();
            testjson.put("password", editTextPW.getText().toString());
            String jsonString = testjson.toString(); //완성된 json 포맷

            //전송
            final RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,testjson, new Response.Listener<JSONObject>() {

                //데이터 전달을 끝내고 응답받기
                @Override
                public void onResponse(JSONObject response) {

                    try {

                        //받은 json형식의 응답을 받아
                        JSONObject jsonObject = new JSONObject(response.toString());

                        //key값에 따라 value값을 쪼개서 받아옴
                        String resultId = jsonObject.getString("approve_id");
                        String resultPassword = jsonObject.getString("approve_pw");
                        //만약 값이 같다면 로그인에 성공
                        if(resultId.equals("OK") & resultPassword.equals("OK")){
                                   Intent intent = new Intent(getApplicationContext(),SubjectActivity.class);
                                   startActivity(intent);
                        }else {
                            Intent intent2 = new Intent(getApplicationContext(),LoginxActivity.class);
                            startActivity(intent2);
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
