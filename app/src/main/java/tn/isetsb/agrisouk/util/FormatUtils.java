package tn.isetsb.agrisouk.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Formats d'affichage français (prix en dinars, dates).
 */
public final class FormatUtils {

    private static final Locale LOCALE = Locale.FRANCE;

    private FormatUtils() {
    }

    public static String price(double value) {
        NumberFormat format = NumberFormat.getInstance(LOCALE);
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);
        return format.format(value) + " DT";
    }

    public static String priceShort(double value) {
        NumberFormat format = NumberFormat.getInstance(LOCALE);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }

    public static String quantity(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return priceShort(value);
    }

    public static String temperature(double value) {
        return String.valueOf(Math.round(value));
    }

    public static String change(double percent) {
        if (Math.abs(percent) < 0.05) {
            return "Stable";
        }
        return String.format(LOCALE, "%s%.1f %%", percent > 0 ? "+" : "-", Math.abs(percent));
    }

    public static String date(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy", LOCALE).format(new Date(timestamp));
    }

    public static String dateLong(long timestamp) {
        String value = new SimpleDateFormat("d MMMM yyyy", LOCALE).format(new Date(timestamp));
        return value;
    }

    public static String dateShortIso(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(isoDate);
            if (date == null) {
                return isoDate;
            }
            String value = new SimpleDateFormat("EEE d MMM", LOCALE).format(date);
            return capitalize(value);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    public static String dayNameIso(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(isoDate);
            if (date == null) {
                return isoDate;
            }
            String value = new SimpleDateFormat("EEEE", LOCALE).format(date);
            return capitalize(value);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    public static String dayMonthIso(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(isoDate);
            if (date == null) {
                return isoDate;
            }
            return new SimpleDateFormat("d MMM", LOCALE).format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    public static String today() {
        String value = new SimpleDateFormat("EEEE d MMMM", LOCALE).format(new Date());
        return capitalize(value);
    }

    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public static String timeOf(String isoDateTime) {
        if (isoDateTime == null || !isoDateTime.contains("T")) {
            return "--:--";
        }
        String[] parts = isoDateTime.split("T");
        return parts.length > 1 ? parts[1].substring(0, Math.min(5, parts[1].length())) : "--:--";
    }
}
