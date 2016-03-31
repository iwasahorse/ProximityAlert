package com.homework.pavement.proximityalert;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    LocationManager locManager;
    AlertReceiver receiver;
    PendingIntent proximityIntent;
    TextView textLatitude;
    TextView textLongitude;
    EditText editLatitude;
    EditText editLongitude;

    LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            textLatitude.setText("" + location.getLatitude());
            textLongitude.setText("" + location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        textLatitude = (TextView) findViewById(R.id.text_latitude);
        textLongitude = (TextView) findViewById(R.id.text_longitude);
        editLatitude = (EditText) findViewById(R.id.edit_latitude);
        editLongitude = (EditText) findViewById(R.id.edit_longitude);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        // 자원 사용 해제
        try {
            locManager.removeUpdates(locationListener);
            if(proximityIntent != null && receiver != null) {
                locManager.removeProximityAlert(proximityIntent);
                unregisterReceiver(receiver);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.apply) {
            // 근접 경보를 받을 브로드캐스트 리시버 객체 생성 및 등록
            // 액션이 kr.ac.koreatech.msp.locationAlert인 브로드캐스트 메시지를 받도록 설정
            receiver = new AlertReceiver();
            IntentFilter filter = new IntentFilter("kr.ac.koreatech.msp.locationAlert");
            registerReceiver(receiver, filter);

            // ProximityAlert 등록을 위한 PendingIntent 객체 얻기
            Intent intent = new Intent("kr.ac.koreatech.msp.locationAlert");
            proximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            try {
                // 근접 경보 등록 메소드
                // void addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent)
                // 아래 위도, 경도 값의 위치는 4공학관 A동

                //Double.parseDouble
                locManager.addProximityAlert(36.761310, 127.279881, 1500, -1, proximityIntent);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

}
