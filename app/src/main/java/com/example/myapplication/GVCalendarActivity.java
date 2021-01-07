package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.ContentValues.TAG;

public class GVCalendarActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener
{

    public static int SUNDAY        = 1;
    public static int MONDAY        = 2;
    public static int TUESDAY       = 3;
    public static int WEDNSESDAY    = 4;
    public static int THURSDAY      = 5;
    public static int FRIDAY        = 6;
    public static int SATURDAY      = 7;

    private static TextView mTvCalendarTitle;
    private GridView mGvCalendar;


    private static ArrayList<DayInfo> mDayList;  //item
    private CalendarAdapter mCalendarAdapter;

    Calendar mLastMonthCalendar;
    Calendar mThisMonthCalendar;
    Calendar mNextMonthCalendar;

    private int sample;
    public int a;

    private RequestQueue mQueue;

    public int lastMonthStartDay;
    public int dayOfMonth;
    public int thisMonthLastDay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_calendar_activity);

        Button bLastMonth = (Button)findViewById(R.id.gv_calendar_activity_b_last);
        Button bNextMonth = (Button)findViewById(R.id.gv_calendar_activity_b_next);

        mTvCalendarTitle = (TextView)findViewById(R.id.gv_calendar_activity_tv_title);
        mGvCalendar = (GridView)findViewById(R.id.gv_calendar_activity_gv_calendar);

        bLastMonth.setOnClickListener(this);
        bNextMonth.setOnClickListener(this);
        mGvCalendar.setOnItemClickListener(this);

        mDayList = new ArrayList<DayInfo>();

        mQueue = Volley.newRequestQueue(this);
    }

    protected void onResume()
    {
        super.onResume();

        mThisMonthCalendar = Calendar.getInstance();
        mThisMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        getCalendar(mThisMonthCalendar);

    }

    protected void getCalendar(Calendar calendar) {
       String url = "http://192.168.43.218:3000/process/ancalendar";


        final JSONObject test_json = new JSONObject();
        String[] subject = SubjectActivity.subject_NUM;
        int position = SubjectActivity.position_num;
        String calendar2 = LoginActivity.ID;

        mDayList.clear();

        // 이번달 시작일의 요일을 구한다. 시작일이 일요일인 경우 인덱스를 1(일요일)에서 8(다음주 일요일)로 바꾼다.)
        dayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
        thisMonthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        calendar.add(Calendar.MONTH, -1);

        // 지난달의 마지막 일자를 구한다.
        lastMonthStartDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        calendar.add(Calendar.MONTH, 1);



        if (dayOfMonth == SUNDAY) {
            dayOfMonth += 7;
        }
        lastMonthStartDay -= (dayOfMonth - 1) - 1;


        // 캘린더 타이틀(년월 표시)을 세팅한다.
        mTvCalendarTitle.setText(mThisMonthCalendar.get(Calendar.YEAR) + "년 "
                + (mThisMonthCalendar.get(Calendar.MONTH) + 1) + "월");

        sample = mThisMonthCalendar.get(Calendar.MONTH) + 1;

       try {
            //서버에 보내는 값 ( 달, 과목번호, 아이디)
            test_json.put("month", sample);
            test_json.put("subject", subject[position]);
            test_json.put("id", calendar2);

            final String jsonString = test_json.toString();
            final RequestQueue requestQueue = Volley.newRequestQueue(com.example.myapplication.GVCalendarActivity.this);

            DayInfo day;

            a = 0;

            //달력모양을 나타내는 for문(3개)
            for (int i = 0; i < dayOfMonth - 1; i++) {
                int date = lastMonthStartDay + i;  // 직전 달의 마지막요일을 표현한다.
                day = new DayInfo();
                day.setDay(Integer.toString(date));
                day.setInMonth(false);
                mDayList.add(day);
                a= a+1;
            }

            for (int i = 1; i <= thisMonthLastDay; i++)  // 현재 달의 요일들을 표현한다.
            {

                day = new DayInfo();
                day.setDay(Integer.toString(i));
                day.setInMonth(true);
                mDayList.add(day);
            }

            for (int i = 1; i < 42 - (thisMonthLastDay + dayOfMonth - 1) + 1; i++) {
                day = new DayInfo();
                day.setDay(Integer.toString(i));
                day.setInMonth(false);
                mDayList.add(day);
            }
            initCalendarAdapter();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, test_json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONArray jsonArray = response.getJSONArray("rows");
                        int length = jsonArray.length();
                        List<String> listContents = new ArrayList<String>(length);

                        DayInfo day;

                        for (int i = 1; i <=thisMonthLastDay; i++)
                        {
                            JSONObject RowDataPacket = jsonArray.getJSONObject(i-1);

                            //월,상태값을 서버로 부터 받아온다.
                            String att_day = RowDataPacket.getString("att_day");
                            String state = RowDataPacket.getString("state");

                            day = new DayInfo();

                            if (state.equals("0"))
                            {
                                day.setDay(att_day + "\n" + "출석");
                                day.setInMonth(true);
                                mDayList.set(a,day);
                                mCalendarAdapter.notifyDataSetChanged();
                                a=a+1;

                            } else if(state.equals("1")) {
                                day.setDay(att_day + "\n" + "결석");
                                day.setInMonth(true);
                                mDayList.set(a,day);
                                mCalendarAdapter.notifyDataSetChanged();
                                a=a+1;

                            } else {
                                day.setDay(att_day + "\n" + "");
                                day.setInMonth(true);
                                mDayList.set(a,day);
                                mCalendarAdapter.notifyDataSetChanged();
                                a=a+1;
                            }

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
    } // 이괄호 지우면 안된다.


    private void initCalendarAdapter() {
        mCalendarAdapter = new CalendarAdapter(this, R.layout.day, mDayList);
        mGvCalendar.setAdapter(mCalendarAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.gv_calendar_activity_b_last:
                mThisMonthCalendar = getLastMonth(mThisMonthCalendar);
                getCalendar(mThisMonthCalendar);
                break;
            case R.id.gv_calendar_activity_b_next:
                mThisMonthCalendar = getNextMonth(mThisMonthCalendar);
                getCalendar(mThisMonthCalendar);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(GVCalendarActivity.this, ImageUploadActivity.class);
        startActivity(intent);

    }

    private Calendar getLastMonth(Calendar calendar)
    {
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        calendar.add(Calendar.MONTH, -1);
        mTvCalendarTitle.setText(mThisMonthCalendar.get(Calendar.YEAR) + "년 "
                + (mThisMonthCalendar.get(Calendar.MONTH) + 1) + "월");
        return calendar;
    }
    private Calendar getNextMonth(Calendar calendar)
    {
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        calendar.add(Calendar.MONTH, +1);
        mTvCalendarTitle.setText(mThisMonthCalendar.get(Calendar.YEAR) + "년 "
                + (mThisMonthCalendar.get(Calendar.MONTH) + 1) + "월");
        return calendar;
    }
}
