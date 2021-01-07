package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SubjectActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    ArrayList<String> items;
    ListView listView;

    public static int position_num;
    private RequestQueue mQueue;
    public static String[] subject_NUM = new String[21];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subject);

        listView =(ListView) findViewById(R.id.list_view);
        items = new ArrayList<String>();
        adapter = new ArrayAdapter(this, R.layout.item_listview,R.id.subject_name,items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SubjectActivity.this, GVCalendarActivity.class);
                position_num = position;
                startActivity(intent);
            }
        });
        mQueue = Volley.newRequestQueue(this);
        request();
    }

    public void request() {
        String url = "http://192.168.43.218:3000/process/ansubject";



        final String subject = "subject";
        String subject2 = LoginActivity.ID;

        JSONObject testjson = new JSONObject();

        try {
            testjson.put(subject, subject2);
            String jsonString = testjson.toString();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, testjson, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONArray jsonArray = response.getJSONArray("rows");
                        int length = jsonArray.length();
                        List<String> listContents = new ArrayList<String>(length);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject RowDataPacket = jsonArray.getJSONObject(i);
                            String subject_name = RowDataPacket.getString("subject_name");  //
                            String subject_number= RowDataPacket.getString("id_subject");

                            items.add(subject_name);
                            subject_NUM[i] = subject_number;
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            mQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
