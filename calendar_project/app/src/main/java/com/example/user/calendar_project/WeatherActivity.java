package com.example.user.calendar_project;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.support.constraint.Constraints.TAG;

public class WeatherActivity extends AppCompatActivity implements GetAPIAndParse.MyInterface{
    ArrayList<Forecast> theWeathers = new ArrayList<>();
    Button              getForecast;
    SimpleDateFormat    format      = new SimpleDateFormat("yyyy/MM/dd");
    Date                time        = new Date();
    String              date        = format.format(time);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getForecast = (Button) findViewById(R.id.buttonGetWeather);

        if (isConnected() == true) {
            GetAPIAndParse getTheWeather = new GetAPIAndParse(WeatherActivity.this, WeatherActivity.this);
            getTheWeather.execute();
            getForecast.setEnabled(false);

        } else {
            if (isConnected() == false) {
                Toast.makeText(WeatherActivity.this, "Connect to the Internet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WeatherActivity.this, "Enter a City", Toast.LENGTH_SHORT).show();
            }
        }

    }
    //----------------------------------------------------------------------------------------------------------------------------------------


    // 오늘 날짜 기준 다음날 구하는 함수
    private String nextDate(String date) throws ParseException {
        SimpleDateFormat  sdf = MainActivity.format;
        Calendar          c   = Calendar.getInstance();
        Date              d   = sdf.parse(date);

        c.setTime(d);
        c.add(Calendar.DATE,1);

        date = sdf.format(c.getTime());
        return date;
    }

    // "/" 기준으로 자르는 함수
    private String splitDate(String date){
        int idx = date.indexOf("/");

        String result = date.substring(idx+1);

        return result;
    }
    //----------------------------------------------------------------------------------------------------------------------------------------


    private boolean isConnected(){
        ConnectivityManager     connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo             networkInfo         = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() || (networkInfo.getType() != ConnectivityManager.TYPE_WIFI &&
                networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    @Override
    public void sendWeather(final ArrayList<Forecast> weatherList) {
        theWeathers = weatherList;

        ListView weatherListView = (ListView)findViewById(R.id.listViewWeather);
        weatherListView.setVisibility(View.VISIBLE);

        getForecast.setEnabled(true);

        WeatherAdapter weatherAdapter = new WeatherAdapter(WeatherActivity.this, R.layout.list_weather, theWeathers);
        weatherListView.setAdapter(weatherAdapter);

        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent myIntent = new Intent(WeatherActivity.this, Details.class);
                myIntent.putExtra("weather", theWeathers.get(i));
                startActivity(myIntent);
            }
        });

        // 차트 그리기
        LineChart        lineChart  = (LineChart)findViewById(R.id.chart);
        ArrayList<Entry> entries    = new ArrayList<>(); // 그래프 위의 지점들을 담아놓은 ArrayList

        entries.add(new Entry(getAvg(theWeathers.get (3).minTemp,theWeathers.get(3).maxTemp)-273,0));    //(y좌표, x좌표)    : val(int)에 변수를 넣어야 함
        entries.add(new Entry(getAvg(theWeathers.get(11).minTemp,theWeathers.get(11).maxTemp)-273,1));
        entries.add(new Entry(getAvg(theWeathers.get(19).minTemp,theWeathers.get(19).maxTemp)-273,2));
        entries.add(new Entry(getAvg(theWeathers.get(27).minTemp,theWeathers.get(27).maxTemp)-273,3));
        entries.add(new Entry(getAvg(theWeathers.get(35).minTemp,theWeathers.get(35).maxTemp)-273,4));

        LineDataSet       lineDataSet = new LineDataSet(entries, "Temperature");
        ArrayList<String> labels      = new ArrayList<String>(); // x축 라벨링

        labels.add(splitDate(date));    //오늘
        try {                           //2일째
            labels.add(splitDate(nextDate(date)));
            date= nextDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {                           //3일째
            labels.add(splitDate(nextDate(date)));
            date = nextDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {                           //4일째
            labels.add(splitDate(nextDate(date)));
            date= nextDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {                           //5일째
            labels.add(splitDate(nextDate(date)));
            date= nextDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 그래프 그리는 부분
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setCircleColorHole(Color.BLUE);
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        LineData lineData = new LineData(labels,lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 20, 0);

        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        lineChart.invalidate();
    }

    private float getAvg(String min, String max){
        float avgTemp = (Float.parseFloat(min)+Float.parseFloat(max))/2;

        return avgTemp;
    }
}
