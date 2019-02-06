package com.example.user.calendar_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;

import static android.support.constraint.Constraints.TAG;

//날씨정보를 보내주는 AsyncTask
public class ReceiveWeatherTask extends AsyncTask<String, Void, JSONObject> {
    String iconName     ="";
    String nowTemp      ="";
    String maxTemp      ="";
    String minTemp      ="";

    String humidity     ="";
    String speed        ="";
    String main         ="";
    String description  ="";
    Bitmap bitmap;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... datas) {
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(datas[0]).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream         is      = conn.getInputStream();
                InputStreamReader   reader  = new InputStreamReader(is);
                BufferedReader      in      = new BufferedReader(reader);

                String readed;

                while ((readed = in.readLine())!= null){
                    JSONObject jObject = new JSONObject(readed);
                    //jObject.getJSONArray("weather).getJSONObject(0).getString("icon");
                    return jObject;
                }
            }else{
                return null;
            } // if~else()

            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } // try~catch
        return null;
    }// doInBackground()

    @Override
    protected void onPostExecute(JSONObject result) {
        if ( result != null){
            try {
                iconName    = result.getJSONArray("weather").getJSONObject(0).getString("icon");
                nowTemp     = result.getJSONObject("main").getString("temp");
                humidity    = result.getJSONObject("main").getString("humidity");
                minTemp     = result.getJSONObject("main").getString("temp_min");
                maxTemp     = result.getJSONObject("main").getString("temp_max");
                speed       = result.getJSONObject("wind").getString("speed");
                main        = result.getJSONArray("weather").getJSONObject(0).getString("main");
                description = result.getJSONArray("weather").getJSONObject(0).getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 켈빈으로 반환되는 온도 데이터를 섭씨로 변환
            double setTemp  = Math.round(Float.parseFloat(nowTemp)) - 273.15;
            double setMax   = Math.round(Float.parseFloat(maxTemp)) - 273.15;
            double setMin   = Math.round(Float.parseFloat(minTemp)) - 273.15;
            description     = transferWeather(description);

            final String msg = description + "\n습도 : " + humidity + "%\n풍속 : " + speed + " m/s"
                    + "\n현재 온도 : " + String.valueOf(String.format("%.1f", setTemp)) + "도\n최저 온도: " + String.valueOf(String.format("%.1f", setMin))
                    + "도\n최고 온도 : " + String.valueOf(String.format("%.1f", setMax)) + "도";

            MainActivity.side_tv_weather.setText(msg);

            Thread imThread = new Thread() {
                @Override
                public void run() {
                    try {
                        URL               url  = new URL("http://openweathermap.org/img/w/" + iconName + ".png");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        conn.setDoInput(true);//응답수신
                        conn.connect();

                        InputStream is = conn.getInputStream(); // InputStream 값 가져오기
                        bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            };

            imThread.start();

            try {
                imThread.join();
                MainActivity.weather_icon.setImageBitmap(bitmap);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // if()
    } // onPostExecute()


    private String transferWeather( String weather ){
        weather = weather.toLowerCase();

        if ( weather.equals("haze")||weather.equals("fog") ){
            return "안개";
        }else if ( weather.equals("clouds") ){
            return "구름";
        }
        else if (weather.equals("rain") ){
            return "비";
        }else if ( weather.equals("few clouds")){
            return "구름 낌";
        }else if ( weather.equals("scattered clouds")){
            return "구름 많음";
        }else if ( weather.equals("overcast clouds")){
            return "맑음";
        }
        return "";
    }
}
