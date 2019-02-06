package com.example.user.calendar_project;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Alarm_result extends AppCompatActivity {
    SQLiteDatabase  db;
    TextView        tv_message;

    String dbName = "MyDB.db3";
    String dbPath = "/friend_database5";

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_result);

        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(1000);

        tv_message = findViewById(R.id.tv_message);

        db = openOrCreateDatabase(Environment.getExternalStorageDirectory().toString() + dbPath + "/"+ dbName,
                SQLiteDatabase.CREATE_IF_NECESSARY, null);
        String query = String.format("select date,schedule,time from Calendar where date = '%s' order by time", MainActivity.date);
        searchQuery(query);
    }

    private void searchQuery(String query){
        Cursor c            = db.rawQuery(query ,  null); // 쿼리 실행
        String result_str   = "";
        String [] col       = new String [c.getColumnCount()];
                  col       = c.getColumnNames();
        String []str        = new String[c.getColumnCount()];

        while(c.moveToNext()){
            for ( int i = 0 ; i< c.getColumnCount() ; i++){
                str[i] = "";
                str[i] += c.getString(i);
                result_str += String.format("%s:%s\n", col[i], str[i] );
            }//for()

            result_str+="\n"; // 한 명 정보 누적이 완료되면 한 번 더 개행
        }//while()

        tv_message.setText(result_str);
    }
}
