package com.example.user.calendar_project;
import android.Manifest;
import android.annotation.SuppressLint;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    //DB연결 멤버변수
    String dbName   = "MyDB.db3";
    String dbPath   = "/friend_database5"; //바꿧어요 4에서
    boolean isFirst = true; // true면 최초 실행 false면 최초 아님(DB가 폰 내에 복사된 상태)
    SQLiteDatabase db;

    //메인 멤버변수
    CalendarView                    cal;
    TextView                        result_schedule,result_moneybook,title_schedule,title_moneybook;
    ArrayList<CalendarVO>           list    = new ArrayList<>();
    CalendarVO                      vo;
    Button                          btn_add;
    static  SimpleDateFormat        format  = new SimpleDateFormat("yyyy/MM/dd");
    static  Date                    time    = new Date();
    static  String                  date    = format.format(time);

    //위치정보 멤버변수
    private final int   PERMISSIONS_ACCESS_FINE_LOCATION    = 1000;
    private final int   PERMISSIONS_ACCESS_COARSE_LOCATION  = 1001;
    private boolean     isAccessFineLocation                = false;
    private boolean     isAccessCoarseLocation              = false;
    private boolean     isPermission                        = false;
    static Double       mylat;
    static Double       mylon;


    // 디데이 변수
    TextView tv_d_day;
    private int tYear, tMonth, tDay;
    private int dYear = 1, dMonth = 1, dDay = 1;
    private long d, t, r;
    private int resultNumber = 0;

    // 알람 변수
    AlarmManager        mManager;
    NotificationManager mNotification;
    Button              btn_on, btn_off;
    Calendar            alarm               = Calendar.getInstance();
    boolean             alarmException      = false;
    /////////////////////////////////////////////////////////////////////
    //Menu 슬라이드 버튼
    Button side_btn_weather, side_btn_allschedule, side_btn_moneybook,side_btn_forecast,
            side_btn_food,side_btn_traffic,side_btn_culture;
    TextView side_tv_allschedule, side_tv_title, side_tv_moneybook,
            side_tv_allfood,side_tv_alltraffic,side_tv_allculture,side_tv_moneybooklist;
    static TextView side_tv_weather;
    static ImageView weather_icon;
//----------------------------------------------------------------------------------------------------

    @SuppressLint({"WrongConstant"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DB 체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            setPermission();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            setPermission();
            return;
        }
        loadFirst();
        copyAssets();
        saveFirst();
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory().toString() + dbPath + "/" + dbName,
                SQLiteDatabase.CREATE_IF_NECESSARY, null);

//----------------------------------------------------------------------------------------------------

        // 메뉴 슬라이드 버튼, 텍스트
        MenuDrawer menuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        menuDrawer.setContentView(R.layout.activity_main);

        LayoutInflater inflater     = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View side_menu_layout       = inflater.inflate(R.layout.activity_side_menu, null);
        weather_icon                = side_menu_layout.findViewById(R.id.weather_icon);
        side_btn_weather            = side_menu_layout.findViewById(R.id.side_btn_weather);
        side_btn_allschedule        = side_menu_layout.findViewById(R.id.side_btn_allschedule);
        side_btn_moneybook          = side_menu_layout.findViewById(R.id.side_btn_moneybook);
        side_btn_forecast           = side_menu_layout.findViewById(R.id.side_btn_forecast);
        side_btn_food               = side_menu_layout.findViewById(R.id.side_btn_food);
        side_btn_traffic            = side_menu_layout.findViewById(R.id.side_btn_traffic);
        side_btn_culture            = side_menu_layout.findViewById(R.id.side_btn_culture);

        side_btn_forecast.setVisibility(View.GONE);
        side_btn_food.setVisibility(View.GONE);
        side_btn_traffic.setVisibility(View.GONE);
        side_btn_culture.setVisibility(View.GONE);

        side_tv_title           = side_menu_layout.findViewById(R.id.side_tv_title);
        side_tv_allschedule     = side_menu_layout.findViewById(R.id.side_tv_allschedule);
        side_tv_weather         = side_menu_layout.findViewById(R.id.side_tv_weather);
        side_tv_moneybook       = side_menu_layout.findViewById(R.id.side_tv_moneybook);
        side_tv_allfood         = side_menu_layout.findViewById(R.id.side_tv_allfood);
        side_tv_alltraffic      = side_menu_layout.findViewById(R.id.side_tv_alltraffic);
        side_tv_allculture      = side_menu_layout.findViewById(R.id.side_tv_allculture);
        side_tv_moneybooklist   = side_menu_layout.findViewById(R.id.side_tv_moneybooklist);

        side_btn_weather.setOnClickListener(click);
        side_btn_allschedule.setOnClickListener(click);
        side_btn_moneybook.setOnClickListener(click);
        side_btn_forecast.setOnClickListener(click);

        side_btn_food.setOnClickListener(click);
        side_btn_traffic.setOnClickListener(click);
        side_btn_culture.setOnClickListener(click);
        menuDrawer.setMenuView(side_menu_layout);

//----------------------------------------------------------------------------------------------------------------------------------------

        //Main화면 버튼, 텍스트
        cal                 = findViewById(R.id.calendarView);
        result_schedule     = findViewById(R.id.result_schedule);
        result_moneybook    = findViewById(R.id.result_moneybook);
        title_schedule      = findViewById(R.id.title_shedule);
        title_moneybook     = findViewById(R.id.title_moneybook);
        btn_add             = findViewById(R.id.btn_add);
        tv_d_day            = findViewById(R.id.tv_d_day);

        // 알람
        mNotification       = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mManager            = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        btn_on              = findViewById(R.id.btn_on);
        btn_off             = findViewById(R.id.btn_off);

//----------------------------------------------------------------------------------------------------------------------------------------
        // 해당날짜 클릭이벤트
        cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                date = Integer.toString(year) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(dayOfMonth);
                if (date != null) { //해당날짜 일정과 가계부 리스트 보여주기
                    String query = String.format("select date,schedule,time from Calendar where date = '%s'", date);
                    searchQuery(query, 1);

                    String moneyquery = String.format("select date,category,money from Calendar where date = '%s' and money!=0", date);
                    searchQuery(moneyquery, 4);
                }

                // 디데이
                dYear = year;
                dMonth = month + 1;
                dDay = dayOfMonth;
                final Calendar dCalendar = Calendar.getInstance();
                dCalendar.set(dYear, dMonth, dDay);
                d = dCalendar.getTimeInMillis();
                r = (d - t) / (24 * 60 * 60 * 1000);
                resultNumber = (int) r - 31;
                updateDisplay();

                ///해당날짜 알람 ON/OFF 이벤트
                alarmException = true;
                alarm.set(dYear, dMonth - 1, dDay, 0, 0, 0); // 시간은 임의로 넣어보세요
                alarmGO();
            }
        });//setOnDateChangeListener()

       //일정추가 클릭이벤트
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, addActivity.class);
                intent.putExtra("date",date); //addActivity에서 해당날짜 넘겨줄려고
                startActivityForResult(intent,1);
            }
        });
        // 디데이
        Calendar calendar = Calendar.getInstance();              //현재 날짜 불러옴
        tYear = calendar.get(Calendar.YEAR);
        tMonth = calendar.get(Calendar.MONTH);
        tDay = calendar.get(Calendar.DAY_OF_MONTH);
        Calendar dCalendar = Calendar.getInstance();
        dCalendar.set(dYear, dMonth, dDay);
        t = calendar.getTimeInMillis();                 //오늘 날짜를 밀리타임으로 바꿈
        d = dCalendar.getTimeInMillis();              //디데이날짜를 밀리타임으로 바꿈
        r = (d - t) / (24 * 60 * 60 * 1000);                 //디데이 날짜에서 오늘 날짜를 뺀 값을 '일'단위로 바꿈
        resultNumber = (int) r;

        // 예외처리
        dbexception(tYear, tMonth);  // 가계부 금액 null값처리와 어플실행시 현재날짜 일정 안보이는거 수정
        if(alarmException == false){ // 알람 예외처리 어플키자마자 현재날짜에 일정이 있는데 알람이 설정안됨 -> 날짜클릭을안했을때
            alarmGO();
        }
    }//onCreate()

//----------------------------------------------------------------------------------------------------------------------------------------
    //알림 띄울 엑티비티
    private PendingIntent pendingIntent() {
        Intent i = new Intent(getApplicationContext(), Alarm_result.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        return pi;
    }
//----------------------------------------------------------------------------------------------------------------------------------------
    //DB
    private void setPermission(){
        TedPermission.with(this).setPermissionListener(permissionListener)
                .setDeniedMessage("저장소 접근 권한을 수락하세요.\n\n[설정] ->[권한]에서 활성화할 수 있습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).check();
    }
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Intent i = new Intent(MainActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            finish();
        }
    };
    private void loadFirst(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        isFirst = pref.getBoolean("save",true); //save 키 값이 없으면 기본값 true (최초)
    }

    private void saveFirst() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("save", isFirst);
        edit.commit();
    }
    private void copyAssets(){
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(dbName);
            String path = Environment.getExternalStorageDirectory().toString();
            String mkdir = path + dbPath; // DB를 저장할 경로
            File fpath = new File(mkdir);
            if(fpath.exists() == false){
                isFirst = true; // 최초로 간주
            }
            if(isFirst){
                fpath.mkdir(); // 경로 생성
                out = new FileOutputStream(mkdir+"/" + dbName);
                byte[] buf  = new byte[10240];
                int    read = 0;
                while( (read = in.read(buf))!= -1 ){// 읽고
                    out.write(buf,0,read);         // 쓰기
                }
                in.close();
                out.flush();
                out.close();
                isFirst = false;     // DB 복사가 완료되면 flag 를 false로 변경
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void searchQuery(String query,int num){
        Cursor c= db.rawQuery(query ,  null); // 쿼리 실행
        String result_str = "";
        String [] col = new String [c.getColumnCount()];
        col = c.getColumnNames();
        String []str = new String[c.getColumnCount()];
        while(c.moveToNext() ){
            for ( int i = 0 ; i< c.getColumnCount() ; i++){
                str[i] = "";
                str[i] +=c.getString(i);
                result_str += String.format("%s : %s\n", col[i], str[i] );
            }//for()
            result_str+="\n"; // 한 명 정보 누적이 완료되면 한 번 더 개행
        }//while()
        if(num == 1){ result_schedule.setText(result_str);  }  //해당 날짜 일정
        if(num == 2){ side_tv_allschedule.setText(result_str); }//해당 월 일정 리스트
        if(num == 3){
            side_tv_moneybook.setText(result_str.contains("null")?result_str.replace("null","0"):result_str);   } // 총금액
        if(num == 4){ result_moneybook.setText(result_str); } //해당 날짜 가계부
        if(num == 5){
            side_tv_allfood.setText(result_str.contains("null")?result_str.replace("null","0"):result_str);     } //총식비
        if(num == 6){
            side_tv_alltraffic.setText(result_str.contains("null")?result_str.replace("null","0"):result_str);   } //총교통비
        if(num == 7){
            side_tv_allculture.setText(result_str.contains("null")?result_str.replace("null","0"):result_str);   } //총문화생활비
        if(num == 8){ side_tv_moneybooklist.setText(result_str);} //월 식비리스트
        if(num == 9){ side_tv_moneybooklist.setText(result_str);} //월 교통비리스트
        if(num == 10){side_tv_moneybooklist.setText(result_str);} //월 문화생활비리스트
    }

//----------------------------------------------------------------------------------------------------------------------------------------

    //일정추가를 저장하면 일로옴
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == 10) {
            switch (requestCode) {
                case 1:
                    //addActivity에서 데이터 가져온것들

                    Bundle bundle = data.getExtras();
                    date = bundle.getString("date");
                    int money = bundle.getInt("money");
                    String schedule = bundle.getString("anniversary");
                    String time = bundle.getString("time");
                    String category = bundle.getString("category");
                    // 가져온거 VO에 넣기
                    vo = new CalendarVO();
                    vo.setDate(date);
                    vo.setMoney(money);
                    vo.setSchedule(schedule);
                    vo.setCategory(category);
                    vo.setTime(time);
                    list.add(vo);

                    // DB
                    ContentValues values = new ContentValues();
                    values.put("date",vo.getDate() );
                    values.put("schedule",vo.getSchedule() );
                    values.put("category",vo.getCategory() );
                    values.put("money",vo.getMoney() );
                    values.put("time",vo.getTime());
                    db.insert("Calendar",null,values);

                    String query = String.format("select date,schedule,time from Calendar where date = '%s'", date);
                    searchQuery(query, 1);
                    if(vo.getMoney()!=0) { //돈 입력안하면
                        String moneyquery = String.format("select date,category,money from Calendar where date = '%s'", date);
                        searchQuery(moneyquery, 4);
                    }
                    break;
            }//switch()
        }//if()
    }//onActivityResult()
//----------------------------------------------------------------------------------------------------------------------------------------
    // 사이드 슬라이드 메뉴 버튼 클릭 이벤트
    Button.OnClickListener click = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            setVisibility_control();
            switch (v.getId()){
                case R.id.side_btn_weather: //UPDATE 2019/01/12
                    side_tv_title.setText("현재날씨");
                    side_tv_weather.setVisibility(View.VISIBLE);
                    weather_icon.setVisibility(View.VISIBLE);
                    side_btn_forecast.setVisibility(View.VISIBLE);
                    if(!isPermission){ callPermission(); }
                    GpsInfo gps = new GpsInfo(MainActivity.this);
                    if(gps.isGetLocation()){
                        double lat = gps.getLat();
                        double lon = gps.getLon();
                        mylat = lat;
                        mylon = lon;
                        getWeatherData(lat,lon); //호출횟수 신경쓰기 주석뺏고 넣고
                    }
                    break;
                case R.id.side_btn_allschedule:   // 해당 월의 일정표를 보여줌 (조건: 달력의 날짜를 클릭하고 슬라이드화면에서 클릭할것)
                    side_tv_title.setText(dMonth+"월 일정표");
                    side_tv_allschedule.setVisibility(View.VISIBLE);
                    String query2 = String.format("select date,schedule,time from Calendar where date like '"+dYear+"/"+dMonth+"%%' order by date");
                    searchQuery(query2,2);
                    break;
                case R.id.side_btn_forecast:
                    Intent i = new Intent(MainActivity.this, WeatherActivity.class);
                    startActivity(i);
                    break;
                case R.id.side_btn_moneybook:     // 해당 월의 가계부를 보여줌 (조건: 달력의 날짜를 클릭해야됨 슬라이드화면에서 클릭할것)
                    side_tv_title.setText(dMonth+"월 가계부");
                    setVisibility_moneybook_ON();
                    String query3 = String.format("select sum(money) as '총지출' from Calendar where date like '"+dYear+"/"+dMonth+"%%'");
                    searchQuery(query3,3);
                    String food_query = String.format("select sum(money) as '식비' from Calendar where date like '"+dYear+"/"+dMonth+"%%' and category='식비'");
                    searchQuery(food_query,5);
                    String traffic_query = String.format("select sum(money) as '교통비' from Calendar where date like '"+dYear+"/"+dMonth+"%%' and category='교통비'");
                    searchQuery(traffic_query,6);
                    String culture_query = String.format("select sum(money) as '문화생활' from Calendar where date like '"+dYear+"/"+dMonth+"%%' and category='문화생활'");
                    searchQuery(culture_query,7);
                    break;
                case R.id.side_btn_food://///////////////월 식비 리스트
                    setVisibility_moneybook_ON();
                    String foodListQuery = String.format
                            ("select date,category,money from Calendar where date like '"+dYear+"/"+dMonth+"%%' and category='식비' and money!=0");
                    searchQuery(foodListQuery,8);
                    break;
                case R.id.side_btn_traffic:         // 월 교통비
                    setVisibility_moneybook_ON();
                    String trafficListQuery = String.format
                            ("select date,category,money from Calendar where date like '"+dYear+"/"+dMonth+"%%' and category='교통비' and money!=0");
                    searchQuery(trafficListQuery,9);
                    break;
                case R.id.side_btn_culture:         // 월 문화생활비
                    setVisibility_moneybook_ON();
                    String cultureListQuery = String.format
                            ("select date,category,money from Calendar where date like '"+dYear+"/"+dMonth+"%%' and category='문화생활' and money!=0");
                    searchQuery(cultureListQuery,10);
                    break;
            }//switch()
        }//onClick()
    };//OnClickListener()

// ----------------------------------------------------------------------------------------------------------------------------------------
    // 날씨API에 위도, 경도 전달
    private void getWeatherData(double lat, double lon){
        String url =
                "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&APPID=9659a0262920d7f78cf765d78fc4e942";
//       String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&APPID=fa12be1f8893a327675e807f7bafd023";
        ReceiveWeatherTask receiveWeather = new ReceiveWeatherTask();
        receiveWeather.execute(url);
    }
//-----------------------------------------------------------------------------------------------------------------------------------------
    //디데이함수
    private void updateDisplay(){
        if(resultNumber >= 0){
            tv_d_day.setText(String.format("D-%d", resultNumber));
        }
        else{
            int absR = Math.abs(resultNumber);
            tv_d_day.setText(String.format("D+%d", absR));
        }
    }//updateDisplay()

//-----------------------------------------------------------------------------------------------------------------------------------------
    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isAccessCoarseLocation = true;
        }
        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }
//-----------------------------------------------------------------------------------------------------------------------------------------
    //사이드 버튼,텍스트 View 제어
    public void setVisibility_control(){
        side_tv_weather.setVisibility(View.GONE);
        weather_icon.setVisibility(View.GONE);
        side_tv_allschedule.setVisibility(View.GONE);
        side_tv_moneybook.setVisibility(View.GONE);
        side_btn_forecast.setVisibility(View.GONE);
        side_btn_food.setVisibility(View.GONE);
        side_btn_traffic.setVisibility(View.GONE);
        side_btn_culture.setVisibility(View.GONE);
        side_tv_allfood.setVisibility(View.GONE);
        side_tv_alltraffic.setVisibility(View.GONE);
        side_tv_allculture.setVisibility(View.GONE);
        side_tv_moneybooklist.setVisibility(View.GONE);
    }

    //가계부 버튼,텍스트 View 제어
    public void setVisibility_moneybook_ON(){
        side_btn_food.setVisibility(View.VISIBLE);
        side_btn_traffic.setVisibility(View.VISIBLE);
        side_btn_culture.setVisibility(View.VISIBLE);
        side_tv_moneybook.setVisibility(View.VISIBLE);
        side_tv_allfood.setVisibility(View.VISIBLE);
        side_tv_alltraffic.setVisibility(View.VISIBLE);
        side_tv_allculture.setVisibility(View.VISIBLE);
        side_tv_moneybooklist.setVisibility(View.VISIBLE);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------
    // 가계부, 일정 예외처리
    public void dbexception(int Year,int Month){
        Month+=1;
        dYear = Year;
        dMonth = Month;
        String query = String.format("select date,schedule,time from Calendar where date = '%s'", date);
        searchQuery(query, 1);
        String moneyquery = String.format("select date,category,money from Calendar where date = '%s' and money!=0", date);
        searchQuery(moneyquery, 4);
    }
    //알람 예외처리
//-----------------------------------------------------------------------------------------------------------------------------------------
    public void alarmGO(){
        btn_on.setOnClickListener(new View.OnClickListener() { //현재시간보다 이전날짜의 일정을 알람on하면 바로뜸
            @Override
            public void onClick(View v) {
                if (result_schedule.getText().length() != 0) {
//                           if( System.currentTimeMillis()>alarm.getTimeInMillis() ){
//                                Toast.makeText(getApplicationContext(),
//                                        "현재시간 이전엔 알람 맞출 수 없어요", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
                    mManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent());
                    Toast.makeText(getApplicationContext(), "알람설정완료.", Toast.LENGTH_SHORT).show();
                    Log.i("HelloAlarmActivity",alarm.getTime().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "일정을 등록하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.cancel(pendingIntent());
                Toast.makeText(getApplicationContext(),"알람OFF",Toast.LENGTH_SHORT).show();
            }
        });
    }
}//MainActivity()

