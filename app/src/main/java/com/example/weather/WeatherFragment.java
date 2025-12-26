package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherFragment extends Fragment {

    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView weatherDescTextView;

    private static final String API_KEY = "55f699b5cc176a2b2c1915c64c477261";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";


    private static final String KEY_CITY = "city_name";
    private static final String KEY_TEMP = "temperature";
    private static final String KEY_HUMIDITY = "humidity";
    private static final String KEY_DESC = "description";

    private RequestQueue requestQueue;


    private String savedCity = "Tehran";
    private String savedTemp = "25°";
    private String savedHumidity = "60%";
    private String savedDesc = "آسمان صاف";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        cityNameTextView = view.findViewById(R.id.cityNameTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        humidityTextView = view.findViewById(R.id.humidityTextView);
        weatherDescTextView = view.findViewById(R.id.weatherDescTextView);

        requestQueue = Volley.newRequestQueue(getActivity());


        if (savedInstanceState != null) {
            savedCity = savedInstanceState.getString(KEY_CITY, "Tehran");
            savedTemp = savedInstanceState.getString(KEY_TEMP, "25°C");
            savedHumidity = savedInstanceState.getString(KEY_HUMIDITY, "رطوبت: 60%");
            savedDesc = savedInstanceState.getString(KEY_DESC, "آسمان صاف");


            cityNameTextView.setText(savedCity);
            temperatureTextView.setText(savedTemp);
            humidityTextView.setText(savedHumidity);
            weatherDescTextView.setText(savedDesc);
        } else {

            fetchWeatherData("Tehran");
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_CITY, savedCity);
        outState.putString(KEY_TEMP, savedTemp);
        outState.putString(KEY_HUMIDITY, savedHumidity);
        outState.putString(KEY_DESC, savedDesc);
    }

    public void fetchWeatherData(String cityName) {
        String url = BASE_URL + "?q=" + cityName + ",IR&units=metric&lang=fa&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String city = response.getString("name");


                            JSONObject main = response.getJSONObject("main");
                            double temp = main.getDouble("temp");
                            int humidity = main.getInt("humidity");


                            JSONObject weather = response.getJSONArray("weather")
                                    .getJSONObject(0);
                            String description = weather.getString("description");


                            savedCity = city;
                            savedTemp = String.format("%.0f°", temp);
                            savedHumidity = humidity + "%";
                            savedDesc = description;


                            if (cityNameTextView != null) {
                                cityNameTextView.setText(savedCity);
                                temperatureTextView.setText(savedTemp);
                                humidityTextView.setText(savedHumidity);
                                weatherDescTextView.setText(savedDesc);
                            }


                            sendWeatherNotification(savedCity, savedTemp, savedDesc, savedHumidity);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(),
                                        "خطا در پردازش اطلاعات",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(),
                                    "خطا در دریافت اطلاعات آب و هوا",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void sendWeatherNotification(String city, String temp,
                                         String desc, String humidity) {
        if (getActivity() != null) {
            Intent serviceIntent = new Intent(getActivity(),
                    WeatherNotificationService.class);
            serviceIntent.putExtra("city", city);
            serviceIntent.putExtra("temperature", temp);
            serviceIntent.putExtra("description", desc);
            serviceIntent.putExtra("humidity", humidity);

            getActivity().startService(serviceIntent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}