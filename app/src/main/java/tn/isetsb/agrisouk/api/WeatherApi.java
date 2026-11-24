package tn.isetsb.agrisouk.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.isetsb.agrisouk.model.Weather;
import tn.isetsb.agrisouk.model.WeatherDay;
import tn.isetsb.agrisouk.util.FormatUtils;

/**
 * Consommation de l'API publique Open-Meteo (aucune clé requise).
 * Les données sont mises en cache pour rester consultables hors connexion.
 */
public final class WeatherApi {

    public static final String SOURCE_NAME = "Open-Meteo";
    public static final String ENDPOINT = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=35.038&longitude=9.485"
            + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
            + "&daily=weather_code,temperature_2m_max,temperature_2m_min"
            + "&timezone=Africa%2FTunis&forecast_days=5";

    private static final String PREFS = "agrisouk_weather_cache";
    private static final String KEY_JSON = "json";
    private static final String KEY_TIME = "fetched_at";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(Weather weather, boolean fromCache);

        void onError(String message);
    }

    private WeatherApi() {
    }

    public static void fetch(Context context, Callback callback) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            Weather cached = readCache(appContext);
            try {
                String json = download(ENDPOINT);
                Weather weather = parse(json);
                long now = System.currentTimeMillis();
                weather.setFetchedAt(now);
                weather.setFromCache(false);
                saveCache(appContext, json, now);
                MAIN.post(() -> callback.onSuccess(weather, false));
            } catch (Exception exception) {
                if (cached != null) {
                    cached.setFromCache(true);
                    MAIN.post(() -> callback.onSuccess(cached, true));
                } else {
                    MAIN.post(() -> callback.onError(
                            "Impossible de joindre la météo. Vérifiez votre connexion Internet."));
                }
            }
        });
    }

    public static Weather readCache(Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = preferences.getString(KEY_JSON, null);
        if (json == null) {
            return null;
        }
        try {
            Weather weather = parse(json);
            weather.setFetchedAt(preferences.getLong(KEY_TIME, 0));
            weather.setFromCache(true);
            return weather;
        } catch (Exception exception) {
            return null;
        }
    }

    private static void saveCache(Context context, String json, long fetchedAt) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_JSON, json)
                .putLong(KEY_TIME, fetchedAt)
                .apply();
    }

    private static String download(String endpoint) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("Accept", "application/json");
        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Réponse HTTP " + status);
            }
            try (InputStream stream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }
        } finally {
            connection.disconnect();
        }
    }

    private static Weather parse(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONObject current = root.getJSONObject("current");

        Weather weather = new Weather();
        weather.setTemperature(current.getDouble("temperature_2m"));
        weather.setHumidity(current.optDouble("relative_humidity_2m", 0));
        weather.setWindSpeed(current.optDouble("wind_speed_10m", 0));
        weather.setCode(current.optInt("weather_code", 0));
        weather.setTimeLabel(FormatUtils.timeOf(current.optString("time", "")));

        JSONObject daily = root.getJSONObject("daily");
        JSONArray dates = daily.getJSONArray("time");
        JSONArray codes = daily.getJSONArray("weather_code");
        JSONArray maximums = daily.getJSONArray("temperature_2m_max");
        JSONArray minimums = daily.getJSONArray("temperature_2m_min");

        for (int index = 0; index < dates.length(); index++) {
            weather.getDays().add(new WeatherDay(
                    dates.getString(index),
                    codes.optInt(index),
                    minimums.optDouble(index),
                    maximums.optDouble(index)));
        }
        return weather;
    }
}
