package tn.isetsb.agrisouk.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Mémorise et applique le thème clair ou sombre choisi dans le profil.
 * Le mode est relu au démarrage de l'application par {@link tn.isetsb.agrisouk.AgriSoukApp}.
 */
public final class ThemeManager {

    private static final String PREFS = "agrisouk_settings";
    private static final String KEY_DARK = "dark_mode";

    private ThemeManager() {
    }

    public static void apply(Context context) {
        setNightMode(isDark(context));
    }

    public static boolean isDark(Context context) {
        return preferences(context).getBoolean(KEY_DARK, false);
    }

    public static void setDark(Context context, boolean dark) {
        preferences(context).edit().putBoolean(KEY_DARK, dark).apply();
        setNightMode(dark);
    }

    private static void setNightMode(boolean dark) {
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
