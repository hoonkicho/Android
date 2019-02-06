package com.example.user.calendar_project;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/*
Lukas Newman
GetAPIAndParse.java
Midterm
Emulated on API 25
 */

public class GetAPIAndParse extends AsyncTask<String, Integer, ArrayList> {
    private Context ctx;
    MyInterface DataPasser;
    ProgressDialog myProgress;

    public GetAPIAndParse(Context ctx, MyInterface DataPasser){
        this.ctx = ctx;
        this.DataPasser = DataPasser;
    }

    @Override
    protected ArrayList doInBackground(String... strings) {
        StringBuilder       stringBuilder   = new StringBuilder();
        HttpURLConnection   connection      = null;
        BufferedReader      reader          = null;
        String              response        = null;
        ArrayList<Forecast> someWeather     = new ArrayList<>();

        double lat = MainActivity.mylat;
        double lon = MainActivity.mylon;

        try {
            String urlString = "http://api.openweathermap.org/data/2.5/forecast?&APPID=fa12be1f8893a327675e807f7bafd023&lat="+lat+"&lon="+lon;
            //위도경도 우리가 쓰는 프로젝트에서 넣으면 될듯
            URL url     = new URL(urlString);
            connection  = (HttpURLConnection) url.openConnection();
            connection.connect();
            int code    = connection.getResponseCode();

            if(code == HttpURLConnection.HTTP_OK) {

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                response    = stringBuilder.toString();
                someWeather = weatherJsonParser(response);
            } else {
                Log.d("myLog", "The code is" + code);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return someWeather;
    }

    @Override
    protected void onPreExecute() {
        //Open Dialog here
        myProgress = new ProgressDialog(ctx);
        myProgress.setMessage("Loading Weather...");
        myProgress.show();
    }

    @Override
    protected void onPostExecute(ArrayList arrayList) {
        //Close Dialog here
        myProgress.dismiss();
       DataPasser.sendWeather(arrayList);

    }

    public ArrayList<Forecast> weatherJsonParser(String line){
        ArrayList<Forecast> tempList = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(line);
            JSONArray listArr = root.getJSONArray("list");

            if(listArr.length() != 0) {
                for (int i = 0; i < listArr.length(); i++) {
                    Forecast tempFore = new Forecast();

                    JSONObject currObj = listArr.getJSONObject(i);

                    //Get the Temps and humidity
                    JSONObject currMain = currObj.getJSONObject("main");

                    tempFore.setMinTemp(currMain.getString("temp_min"));
                    Log.d("myLog", "min: " + tempFore.getMinTemp());

                    tempFore.setMaxTemp(currMain.getString("temp_max"));
                    Log.d("myLog", "max: " + tempFore.getMaxTemp());

                    tempFore.setHumidity(currMain.getString("humidity"));
                    Log.d("myLog", "humidity: " + tempFore.getHumidity());

                    //Get the Description
                    JSONArray   currWeather     = currObj.getJSONArray("weather");
                    JSONObject  currDescription = currWeather.getJSONObject(0);

                    tempFore.setDescription(currDescription.getString("description"));

                    StringBuilder iconURL = new StringBuilder("https://openweathermap.org/img/w/");

                    iconURL.append(currDescription.getString("icon"));
                    iconURL.append(".png");
                    tempFore.setIconURL(iconURL.toString());

                    //Get the Wind conditions
                    JSONObject currWind = currObj.getJSONObject("wind");
                    tempFore.setWindSpeed(currWind.getString("speed"));

                    //get the date and time
                    tempFore.setDatetimeText(currObj.getString("dt_txt"));
                    //Add to the list
                    tempList.add(tempFore);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tempList;
    }
    public static interface MyInterface{
        public void sendWeather(ArrayList<Forecast> weatherList);
    }
}
