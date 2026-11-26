package tn.isetsb.agrisouk.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import tn.isetsb.agrisouk.R;

/**
 * Ouvre Google Maps sur le marché de gros de Sidi Bouzid, avec repli sur le navigateur.
 */
public final class MapsLauncher {

    private static final String QUERY = "Marché de gros de Sidi Bouzid, Tunisie";

    private MapsLauncher() {
    }

    public static void openMarket(Context context) {
        String query = Uri.encode(QUERY);
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + query)));
            return;
        } catch (ActivityNotFoundException ignored) {
            // Pas d'application de cartes : on tente le navigateur.
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + query)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(context, R.string.itinerary_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
}
