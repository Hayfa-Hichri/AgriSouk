package tn.isetsb.agrisouk.model;

/**
 * Prévision météo pour une journée (API Open-Meteo).
 */
public class WeatherDay {

    private final String isoDate;
    private final int code;
    private final double minTemperature;
    private final double maxTemperature;

    public WeatherDay(String isoDate, int code, double minTemperature, double maxTemperature) {
        this.isoDate = isoDate;
        this.code = code;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    public String getIsoDate() {
        return isoDate;
    }

    public int getCode() {
        return code;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }
}
