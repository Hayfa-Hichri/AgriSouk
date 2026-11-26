package tn.isetsb.agrisouk.util;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.StringRes;

import java.util.Locale;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.Weather;
import tn.isetsb.agrisouk.model.WeatherDay;

/**
 * Moteur de règles local : transforme les prévisions Open-Meteo en décision agricole.
 */
public final class DailyAdvice {

    public enum Level { GOOD, WARNING, DANGER }

    public static final class Verdict {
        @StringRes
        public final int titleRes;
        @StringRes
        public final int bodyRes;
        public final Object[] args;
        public final Level level;

        Verdict(int titleRes, int bodyRes, Object[] args, Level level) {
            this.titleRes = titleRes;
            this.bodyRes = bodyRes;
            this.args = args;
            this.level = level;
        }

        public String title(Context context) {
            return context.getString(titleRes, args);
        }

        public String body(Context context) {
            return context.getString(bodyRes, args);
        }

        public String speechText(Context context, Locale locale) {
            Context localized = localized(context, locale);
            return localized.getString(titleRes, args) + ". " + localized.getString(bodyRes, args);
        }
    }

    private DailyAdvice() {
    }

    public static Verdict compute(Weather weather) {
        int wind = (int) Math.round(weather.getWindSpeed());
        WeatherDay today = weather.getDays().isEmpty() ? null : weather.getDays().get(0);
        WeatherDay tomorrow = weather.getDays().size() > 1 ? weather.getDays().get(1) : null;

        int min = today != null
                ? (int) Math.round(today.getMinTemperature())
                : (int) Math.round(weather.getTemperature());
        int max = today != null
                ? (int) Math.round(today.getMaxTemperature())
                : (int) Math.round(weather.getTemperature());

        if (min <= 3) {
            return new Verdict(R.string.advice_frost_title, R.string.advice_frost_body,
                    new Object[]{min}, Level.DANGER);
        }
        if (wind >= 20) {
            return new Verdict(R.string.advice_wind_title, R.string.advice_wind_body,
                    new Object[]{wind}, Level.WARNING);
        }
        if (isRainy(today) || isRainy(tomorrow)) {
            return new Verdict(R.string.advice_rain_title, R.string.advice_rain_body,
                    null, Level.WARNING);
        }
        if (max >= 38) {
            return new Verdict(R.string.advice_heat_title, R.string.advice_heat_body,
                    new Object[]{max}, Level.WARNING);
        }
        return new Verdict(R.string.advice_good_title, R.string.advice_good_body,
                null, Level.GOOD);
    }

    public static Context localized(Context context, Locale locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    private static boolean isRainy(WeatherDay day) {
        if (day == null) {
            return false;
        }
        int code = day.getCode();
        return (code >= 51 && code <= 67)
                || (code >= 80 && code <= 82)
                || (code >= 95 && code <= 99);
    }
}
