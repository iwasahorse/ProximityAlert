package com.homework.pavement.proximityalert;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.support.v7.app.AlertDialog.*;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private AlertReceiver receiver;
    private PendingIntent proximityIntent;
    private TextView textLatitude;
    private TextView textLongitude;
    private EditText editLatitude;
    private EditText editLongitude;
    private EditText editName;
    private LinearLayout linearProximity;

    //private static final String TEST_PROVIDER = "TEST_PROVIDER";
    private static int IS_ALARM_REGISTERED = 0;

    private LocationListener locationListener = new LocationListener() {

        //위치 정보가 바뀌면 위치 정보를 나타내는 텍스트가 업데이트 됩니다.
        @Override
        public void onLocationChanged(Location location) {
            textLatitude.setText(String.format("%f", location.getLatitude()));
            textLongitude.setText(String.format("%f", location.getLongitude()));
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
        setContentView(R.layout.activity_main);

        //레이아웃에서 객체 접근이 필요한 뷰들을 참조합니다.
        textLatitude = (TextView) findViewById(R.id.text_latitude);
        textLongitude = (TextView) findViewById(R.id.text_longitude);
        editLatitude = (EditText) findViewById(R.id.edit_latitude);
        editLongitude = (EditText) findViewById(R.id.edit_longitude);
        editName = (EditText) findViewById(R.id.edit_name);
        linearProximity = (LinearLayout) findViewById(R.id.linear_proximity);

        //방송을 송신 또는 수신할 인텐트와 방송 수신자를 설정합니다.
        IntentFilter filter = new IntentFilter("com.homework.pavement.location.alert");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        receiver = new AlertReceiver();
        registerReceiver(receiver, filter);

        //GPS 위치 정보에 대한 주기적인 업데이트를 요청하고 불가능하면 프로그램을 종료합니다.
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
            Builder builder = new Builder(this);
            builder.setMessage("현재 위치를 확인할 수 없습니다.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        /*if (locationManager.getProvider(TEST_PROVIDER) != null)
            locationManager.removeTestProvider(TEST_PROVIDER);
        locationManager.addTestProvider(TEST_PROVIDER, false, false, false, false, false, false, false, 0, 5);
        locationManager.setTestProviderEnabled(TEST_PROVIDER, true);
        locationManager.setTestProviderStatus(TEST_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());*/
    }

    //앱이 종료될 때 근접 경보와 위치 정보 업데이트, 방송 수신 설정을 모두 해제합니다.
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            locationManager.removeUpdates(locationListener);
            if (proximityIntent != null) locationManager.removeProximityAlert(proximityIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        unregisterReceiver(receiver);
    }

    //등록 버튼을 눌렀을 때 동작합니다. 위도, 경도, 장소 이름이 모두 올바르게 입력되었을 때 근접 경보를 등록합니다.
    //기존에 등록되어 있던 근접 경보가 있으면 지우고 다시 등록합니다. 등록이 완료되면 설정 화면을 INVISIBLE 상태로 만들고 플래그를 변경합니다.
    public void onClick(View view) {
        double latitude;
        double longitude;
        String name;

        try {
            latitude = Double.parseDouble(editLatitude.getText().toString());
            longitude = Double.parseDouble(editLongitude.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "잘못된 입력입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        name = editName.getText().toString();
        if (name.equals("")) {
            Toast.makeText(this, "장소 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //인텐트를 통해 전달할 장소 이름과 방송될 펜딩 인텐트를 설정합니다.
        Intent intent = new Intent("com.homework.pavement.location.alert");
        intent.putExtra("name", name);
        proximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

       /* Location loc = new Location(TEST_PROVIDER);
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        loc.setAccuracy(5);
        loc.setTime(System.currentTimeMillis());
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        locationManager.setTestProviderLocation(TEST_PROVIDER, loc);*/

        try {
            locationManager.removeProximityAlert(proximityIntent);
            locationManager.addProximityAlert(latitude, longitude, 30, -1, proximityIntent);
            Toast.makeText(this, String.format("%s 근접 경보가 등록되었습니다.", name), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "근접 경보를 등록할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        linearProximity.setVisibility(View.INVISIBLE);
        IS_ALARM_REGISTERED = 1;

    }

    //경보 설정 버튼을 눌렀을 때 설정창을 활성화합니다.
    public void onClickRegister(View view) {
        linearProximity.setVisibility(View.VISIBLE);
    }

    //등록된 근접 경보가 있으면 경보를 해제하고 플래그를 변경합니다.
    public void onClickUnregister(View view) {
        if (IS_ALARM_REGISTERED == 1) {
            IS_ALARM_REGISTERED = 0;

            try {
                locationManager.removeProximityAlert(proximityIntent);
                Toast.makeText(this, "근접 경보가 해제되었습니다.", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(this, "근접 경보를 해제할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(this, "등록된 근접 경보가 없습니다.", Toast.LENGTH_SHORT).show();

    }

}
