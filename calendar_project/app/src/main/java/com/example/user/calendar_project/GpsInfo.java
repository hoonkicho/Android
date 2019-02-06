package com.example.user.calendar_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

public class GpsInfo extends Service implements LocationListener {
    private final Context mContext;

    //GPS 사용 여부
    boolean isGpsEnabled = false;
    //네트워크 사용 유무
    boolean isNetworkEnabled = false;

    //GPS 상태값
    boolean isGetLocation = false;

    Location location;
    double lat, lon;

    //GPS 정보 업데이트 거리 100m
    private static final long MIN_DISTANCE_CHANGE_UPDATE = 100;

    //최소 GPS 업데이트 시간 텀 10분
    private static final long GPS_UPDATES_TERM_TIME = 1000*60*10;

    protected LocationManager locationManager;

    public GpsInfo(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    public Location getLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        //GPS 정보 가져오기
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //현재 네트워크 상태 값
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //-------------------------------------
        if(!isGpsEnabled && !isNetworkEnabled){
            //GPS, 네트워크 사용 불가능일 때
            // 다이얼로그 창 띄운 후
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
            dialogBuilder.setTitle("Alert");
            dialogBuilder.setMessage("네트워크 혹은 GPS를 사용할 수 없습니다.");

            dialogBuilder.setPositiveButton("메인 화면으로", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 메인 액티비티로 전환
                    Intent i = new Intent(mContext, MainActivity.class);
                    startActivity(i);
                    ((Activity)mContext).finish();  //오류 시 확인
                }
            });
            dialogBuilder.show();

        }

        else{
            this.isGetLocation = true;

            //네트워크 정보로부터 위치값 가져오기
            if(isNetworkEnabled){
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        GPS_UPDATES_TERM_TIME,MIN_DISTANCE_CHANGE_UPDATE,
                        this
                );
            }

            if(locationManager != null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(location != null){
                    //위도 경도 저장하기
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }
            }
        }
        //--------------------------------
        if(isGpsEnabled){
            if(location == null){
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        GPS_UPDATES_TERM_TIME, MIN_DISTANCE_CHANGE_UPDATE,
                        this
                );

                if(locationManager != null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location != null){
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }
                }
            }

        }
        //---------------------------------
        return location;
    }//getLocation()

    //GPS 종료
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GpsInfo.this);
        }
    }

    //위도, 경도 가져오기
    public double getLat(){
        if(location != null){
            lat = location.getLatitude();
        }
        return lat;
    }

    public double getLon(){
        if(location != null){
            lon = location.getLongitude();
        }
        return lon;
    }

    //GPS와 네트워크의 활성화 유무 확인


    public boolean isGetLocation() {
        return isGetLocation;
    }

    //GPS 정보를 가져오지 못했을 때 띄우는 창
    public void showAlert(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS Setting");
        alertDialog.setMessage("GPS 세팅이 설정되지 않았습니다. \n설정 창으로 이동하시겠습니까?");

        //네
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //아니오
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
