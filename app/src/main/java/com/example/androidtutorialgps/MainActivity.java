//
// Модуль 7, урок 1: Android Framework Location API
//      Создать приложение показывающее список(20) последних полученных координат в единый TextView
//      с сохранением списка при смене ориентации экрана.
//
// 35987: Валерий Куликов, 79068017667@yandex.ru
// 2018/10/31
//
//  Примечания:
//  - сохранение данных при повороте производится за счет выделенного объекта данных [GpsDataStorage]
//  - применяется "прокручиваемый" лист - старые данные удаляются
//  - в [TextView] используется прокрутка, иначе не видно всех данных при landscape
//  - добавлена метка времени
//  - добавлена проверка на наличие провайдеров при подписке
//  - форматирование + моноширинный шрифт делают табличку читаемой
//

package com.example.androidtutorialgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtutorialgps.GpsDataStorage.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GpsDataStorage.Listener {

    private static final int PERMISSION_REQUEST_CODE = 1234;

    private SimpleDateFormat mTimestampFmt;

    private TextView mOutput;

    private LocationManager mLocationManager;

    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(final Location location) {
            saveLocation(location);
        }

        public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        }

        public void onProviderEnabled(final String provider) {
        }

        public void onProviderDisabled(final String provider) {
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimestampFmt = new SimpleDateFormat(getString(R.string.fmt_timestamp), Locale.getDefault());

        // Подпишемся
        GpsDataStorage.getInstance().setListener(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mOutput = findViewById(R.id.view_output);
        // Добавим прокруточку
        mOutput.setMovementMethod(new ScrollingMovementMethod());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startTrackingLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
        }

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    throw new RuntimeException(getString(R.string.err_access));
                }
            }
        }

        startTrackingLocation();
    }

    @SuppressWarnings("ResourceType")
    private void startTrackingLocation() {
        if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } else {
            Toast.makeText(this, getString(R.string.err_gps_provider), Toast.LENGTH_SHORT).show();
        }
        if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        } else {
            Toast.makeText(this, getString(R.string.err_network_provider), Toast.LENGTH_SHORT).show();
        }

        final Location lastKnownGpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownGpsLocation != null) {
            saveLocation(lastKnownGpsLocation);
        } else {
            final Location lastKnownNetworkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            saveLocation(lastKnownNetworkLocation);
        }
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void onDestroy() {
        super.onDestroy();

        mLocationManager.removeUpdates(mLocationListener);
    }

    private void saveLocation(final Location location) {
        if (location != null) {
            GpsDataStorage.getInstance().addData(location.getLatitude(), location.getLongitude());
        } else {
            GpsDataStorage.getInstance().addError();
        }
    }

    @Override
    public void onDataChanged(GpsDataStorage sender) {
        final List<Data> arr = sender.getAll();
        final Formatter fmt = new Formatter();
        fmt.format(getString(R.string.fmt_title), getString(R.string.tbl_title_time),
                getString(R.string.title_latitude),
                getString(R.string.title_longitude));
        for (int i = 0; i < arr.size(); ++i) {
            final Data data = arr.get(i);
            final String timestamp = mTimestampFmt.format(new Date(data.timestamp));
            if(data.success){
                fmt.format(getString(R.string.fmt_row), timestamp, data.latitude, data.longitude);
            }else{
                fmt.format(getString(R.string.fmt_bad_row), timestamp,
                        getString(R.string.str_no_data),
                        getString(R.string.str_no_data));
            }
        }
        mOutput.setText(fmt.toString());
    }
}
