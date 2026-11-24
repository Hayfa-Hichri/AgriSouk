package tn.isetsb.agrisouk.util;

import android.content.Context;
import android.content.SharedPreferences;

import tn.isetsb.agrisouk.model.User;

/**
 * Conserve la session de l'utilisateur connecté dans SharedPreferences.
 */
public class SessionManager {

    private static final String PREFS = "agrisouk_session";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ROLE = "user_role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(User user) {
        preferences.edit()
                .putLong(KEY_ID, user.getId())
                .putString(KEY_NAME, user.getFullName())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_ROLE, user.getRole())
                .apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return preferences.getLong(KEY_ID, 0) > 0;
    }

    public long getUserId() {
        return preferences.getLong(KEY_ID, 0);
    }

    public String getFullName() {
        return preferences.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, User.ROLE_USER);
    }

    public boolean isAdmin() {
        return User.ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public String getFirstName() {
        String name = getFullName().trim();
        if (name.isEmpty()) {
            return "";
        }
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    public String getInitials() {
        String name = getFullName().trim();
        if (name.isEmpty()) {
            return "?";
        }
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
