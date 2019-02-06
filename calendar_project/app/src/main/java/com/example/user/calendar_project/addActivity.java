package com.example.user.calendar_project;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class addActivity extends AppCompatActivity {
    EditText    et_money,et_schedule;
    TextView    result_tv;
    Button      btn;
    Bundle      bundle;
    Intent      intent;
    TimePicker  timePicker;
    Spinner     sp_category;
    String      exception = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        result_tv   = findViewById(R.id.result_tv);
        timePicker  = findViewById(R.id.timePicker);
        et_money    = findViewById(R.id.et_money);
        et_schedule = findViewById(R.id.et_schedule);
        sp_category = findViewById(R.id.sp_category);
        btn         = findViewById(R.id.btn);

        Intent resultDate = getIntent();
        result_tv.setText(resultDate.getStringExtra("date")); // 메인에서 받아온 날짜

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //저장버튼은 메인result함수로
                bundle = new Bundle();
                intent = new Intent();

                if (Build.VERSION.SDK_INT < 23) {
                    int getHour   = timePicker.getCurrentHour();
                    int getMinute = timePicker.getCurrentMinute();

                    bundle.putString("time", getHour + ":" + getMinute);

                } else {
                    int getHour   = timePicker.getHour();
                    int getMinute = timePicker.getMinute();

                    bundle.putString("time", getHour + ":" + getMinute);

                }
                bundle.putString("date", result_tv.getText().toString());

                if (et_money.getText().toString().length() == 0) { //금액 안넣었을때 예외처리
                    bundle.putInt("money", Integer.parseInt(exception));

                } else { //금액을 넣었는데
                    try {
                        bundle.putInt("money", Integer.parseInt(et_money.getText().toString()));
                    }
                    catch (NumberFormatException e){ // 숫자가 아니면
                        Toast.makeText(addActivity.this,"숫자를 입력하세요.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (et_schedule.getText().toString().length() == 0) {
                    Toast.makeText(addActivity.this,"일정을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    bundle.putString("anniversary", et_schedule.getText().toString());
                }

                bundle.putString("category", sp_category.getSelectedItem().toString());
                intent.putExtras(bundle);

                setResult(10, intent);
                finish();
            }
        });
    }
}
