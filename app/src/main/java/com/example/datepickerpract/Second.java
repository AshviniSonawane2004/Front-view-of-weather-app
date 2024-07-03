package com.example.datepickerpract;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Second extends AppCompatActivity {

    private LinearLayout weeklyForecastLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Retrieve city name from intent extras
        String city = getIntent().getStringExtra("city");

        // Fetch weekly forecast data
        new FetchWeeklyForecastTask().execute(city);
    }

    private class FetchWeeklyForecastTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String apiKey = "6dc677dca00447d1f965df89aa62c00f"; // Replace with your API key
            String city = params[0];

            try {
                String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                return new JSONObject(response.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weeklyForecastResponse) {
            super.onPostExecute(weeklyForecastResponse);
            if (weeklyForecastResponse != null) {
                try {
                    // Parse weekly forecast data and update UI
                    JSONArray forecastList = weeklyForecastResponse.getJSONArray("list");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault());

                    for (int i = 0; i < forecastList.length(); i++) {
                        JSONObject forecast = forecastList.getJSONObject(i);
                        long timestamp = forecast.getLong("dt") * 1000; // Convert to milliseconds
                        calendar.setTimeInMillis(timestamp);
                        String dayOfWeek = sdf.format(calendar.getTime());
                        String weatherDescription = forecast.getJSONArray("weather").getJSONObject(0).getString("description");
                        String temperature = String.valueOf(Math.round(forecast.getJSONObject("main").getDouble("temp") - 273.15)) + "°C";

                        // Create TextView for each day's forecast
                        TextView dayTextView = new TextView(Second.this);
                        dayTextView.setText(dayOfWeek + ": " + weatherDescription + ", Temp: " + temperature);
                        // Add TextView to the LinearLayout
                        weeklyForecastLinearLayout.addView(dayTextView);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle null result or errors
                TextView errorTextView = new TextView(Second.this);
                errorTextView.setText("Failed to fetch weekly forecast data.");
                weeklyForecastLinearLayout.addView(errorTextView);
            }
        }
    }
}
