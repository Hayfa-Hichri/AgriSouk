package tn.isetsb.agrisouk.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

import tn.isetsb.agrisouk.model.User;
import tn.isetsb.agrisouk.util.SecurityUtils;

/**
 * Inscription, connexion et lecture des utilisateurs.
 */
public class UserRepository {

    private final DatabaseHelper helper;

    public UserRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public User authenticate(String email, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = {"id", "full_name", "email", "phone", "role"};
        try (Cursor cursor = db.query("users", columns,
                "email = ? AND password_hash = ?",
                new String[]{normalize(email), SecurityUtils.hash(password)},
                null, null, null, "1")) {
            if (cursor.moveToFirst()) {
                return new User(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
            }
        }
        return null;
    }

    /**
     * @return l'identifiant du nouvel utilisateur, ou -1 si l'e-mail existe déjà.
     */
    public long register(String fullName, String email, String phone, String password) {
        String normalizedEmail = normalize(email);
        if (emailExists(normalizedEmail)) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put("full_name", fullName.trim());
        values.put("email", normalizedEmail);
        values.put("phone", phone == null ? "" : phone.trim());
        values.put("password_hash", SecurityUtils.hash(password));
        values.put("role", User.ROLE_USER);
        values.put("created_at", System.currentTimeMillis());
        return helper.getWritableDatabase().insertOrThrow("users", null, values);
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE email = ?",
                new String[]{normalize(email)})) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    public User findById(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = {"id", "full_name", "email", "phone", "role"};
        try (Cursor cursor = db.query("users", columns, "id = ?",
                new String[]{String.valueOf(userId)}, null, null, null, "1")) {
            if (cursor.moveToFirst()) {
                return new User(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
            }
        }
        return null;
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
