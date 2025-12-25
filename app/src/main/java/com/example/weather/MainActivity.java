package com.example.weather;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private Spinner citySpinner;
    private Button getWeatherButton;
    private InternetCheckService internetCheckService;
    private NetworkChangeReceiver networkChangeReceiver;

    private String[] iranCities = {
            "Tehran", "Mashhad", "Isfahan", "Shiraz", "Tabriz",
            "Karaj", "Ahvaz", "Qom", "Kermanshah", "Urmia",
            "Rasht", "Kerman", "Zahedan", "Hamedan", "Yazd"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkAndRequestPermissions();


        internetCheckService = new InternetCheckService();
        Intent serviceIntent = new Intent(this, InternetCheckService.class);
        startService(serviceIntent);


        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        initializeViews();


        if (savedInstanceState == null) {
            WeatherFragment weatherFragment = new WeatherFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, weatherFragment)
                    .commit();
        }
    }

    private void initializeViews() {
        citySpinner = findViewById(R.id.citySpinner);
        getWeatherButton = findViewById(R.id.getWeatherButton);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                iranCities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetAvailable()) {
                    String selectedCity = citySpinner.getSelectedItem().toString();
                    WeatherFragment fragment = (WeatherFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);
                    if (fragment != null) {
                        fragment.fetchWeatherData(selectedCity);
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "اتصال به اینترنت برقرار نیست",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        PERMISSION_REQUEST_CODE);
            }
        } else {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE
                        },
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "مجوزها داده شد", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "مجوزها رد شد", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
        Intent serviceIntent = new Intent(this, InternetCheckService.class);
        stopService(serviceIntent);
    }


    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInternetAvailable()) {
                Toast.makeText(context, "اینترنت متصل است", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "اینترنت قطع است", Toast.LENGTH_SHORT).show();
            }
        }
    }
}