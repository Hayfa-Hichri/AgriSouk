package tn.isetsb.agrisouk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Météo courante et prévisions sur 5 jours.
 */
public class Weather {

    private double temperature;
    private double humidity;
    private double windSpeed;
    private int code;
    private String timeLabel;
    private long fetchedAt;
    private boolean fromCache;
    private final List<WeatherDay> days = new ArrayList<>();

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(String timeLabel) {
        this.timeLabel = timeLabel;
    }

    public long getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(long fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public List<WeatherDay> getDays() {
        return days;
    }
}
