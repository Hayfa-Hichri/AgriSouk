package tn.isetsb.agrisouk.util;

import tn.isetsb.agrisouk.R;

/**
 * Libellés et icônes associés aux codes météo WMO de l'API Open-Meteo.
 */
public final class WeatherCodes {

    private WeatherCodes() {
    }

    public static String label(int code) {
        if (code == 0) {
            return "Ciel dégagé";
        } else if (code == 1) {
            return "Généralement dégagé";
        } else if (code == 2) {
            return "Partiellement nuageux";
        } else if (code == 3) {
            return "Couvert";
        } else if (code == 45 || code == 48) {
            return "Brouillard";
        } else if (code >= 51 && code <= 57) {
            return "Bruine";
        } else if (code >= 61 && code <= 67) {
            return "Pluie";
        } else if (code >= 71 && code <= 77) {
            return "Neige";
        } else if (code >= 80 && code <= 82) {
            return "Averses";
        } else if (code == 85 || code == 86) {
            return "Averses de neige";
        } else if (code >= 95) {
            return "Orage";
        }
        return "Nuageux";
    }

    public static int icon(int code) {
        if (code == 0 || code == 1) {
            return R.drawable.ic_sunny;
        } else if (code == 2) {
            return R.drawable.ic_partly_cloudy;
        } else if (code == 3) {
            return R.drawable.ic_cloudy;
        } else if (code == 45 || code == 48) {
            return R.drawable.ic_fog;
        } else if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            return R.drawable.ic_rain;
        } else if ((code >= 71 && code <= 77) || code == 85 || code == 86) {
            return R.drawable.ic_snow;
        } else if (code >= 95) {
            return R.drawable.ic_storm;
        }
        return R.drawable.ic_cloudy;
    }

    /**
     * Conseil agricole simple affiché sous les prévisions.
     */
    public static String advice(int code) {
        if (code == 0 || code == 1) {
            return "Conditions idéales pour les travaux de plein champ.";
        } else if (code == 2) {
            return "Temps calme, bonne journée pour l'irrigation.";
        } else if (code >= 51 && code <= 67) {
            return "Pluie attendue : reportez les traitements.";
        } else if (code >= 80 && code <= 82) {
            return "Averses possibles : protégez les cultures sensibles.";
        } else if (code >= 95) {
            return "Orages : évitez les travaux exposés.";
        } else if (code == 45 || code == 48) {
            return "Brouillard : prudence sur les routes agricoles.";
        }
        return "Surveillez l'humidité de vos parcelles.";
    }
}
