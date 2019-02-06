package com.example.user.calendar_project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/*
Lukas Newman
Details.java
Midterm
Emulated on API 25
 */

public class Details extends AppCompatActivity {
    TextView theMinTemp;
    TextView theMaxTemp;
    TextView theWindSpeed;
    TextView theHumidity;
    TextView detailsfor;
    static float setTemp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setTitle("Details");

        Intent intent = this.getIntent();
        Forecast foreDetails = (Forecast)intent.getSerializableExtra("weather");

        detailsfor = (TextView)findViewById(R.id.textViewDetails);
        theMinTemp = (TextView)findViewById(R.id.textViewMin);
        theMaxTemp = (TextView)findViewById(R.id.textViewMax);
        theWindSpeed = (TextView)findViewById(R.id.textViewWind);
        theHumidity = (TextView)findViewById(R.id.textViewHumidity);
        float setMin = Math.round(Float.parseFloat(foreDetails.getMinTemp())) - 273;
        float setMax = Math.round(Float.parseFloat(foreDetails.getMaxTemp())) - 273;
        setTemp = (setMax+setMin)/2;
        detailsfor.setText("상세정보 " + foreDetails.getDatetimeText());
        theMinTemp.setText("최저온도: " + String.valueOf(String.format("%.1f", setMin))+"도");
        theMaxTemp.setText("최고온도: " + String.valueOf(String.format("%.1f", setMax))+"도");
        theWindSpeed.setText("풍속: " + foreDetails.getWindSpeed()+"m/s");
        theHumidity.setText("습도: " + foreDetails.getHumidity()+"%");

    }
}
