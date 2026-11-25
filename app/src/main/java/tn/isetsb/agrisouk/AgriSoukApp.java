package tn.isetsb.agrisouk;

import android.app.Application;

import tn.isetsb.agrisouk.util.ThemeManager;

/**
 * Applique le thème enregistré avant la création de la première activité.
 */
public class AgriSoukApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(this);
    }
}
