package tn.isetsb.agrisouk.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.model.User;
import tn.isetsb.agrisouk.util.SecurityUtils;

/**
 * Base SQLite locale : utilisateurs, produits, historique des prix et favoris.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "agrisouk.db";
    private static final int DB_VERSION = 1;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT, " +
                "password_hash TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ref TEXT NOT NULL UNIQUE, " +
                "name TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "previous_price REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "image_key TEXT NOT NULL, " +
                "description TEXT, " +
                "updated_at INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE price_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "recorded_at INTEGER NOT NULL, " +
                "FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE favorites (" +
                "user_id INTEGER NOT NULL, " +
                "product_id INTEGER NOT NULL, " +
                "PRIMARY KEY(user_id, product_id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS price_history");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        long adminId = insertUser(db, "Admin AgriSouk", "admin@agrisouk.tn", "71456789",
                SecurityUtils.hash("admin123"), User.ROLE_ADMIN);
        long userId = insertUser(db, "Sami Ben Ali", "user@agrisouk.tn", "98123456",
                SecurityUtils.hash("user123"), User.ROLE_USER);

        long now = System.currentTimeMillis();
        Object[][] data = {
                {"TOM-001", "Tomate", Product.CAT_LEGUMES, 1.850, 1.700, "kg", "tomato",
                        "Tomate fraîche de Sidi Bouzid, récolte du matin, calibre moyen."},
                {"PDT-002", "Pomme de terre", Product.CAT_LEGUMES, 1.300, 1.350, "kg", "potato",
                        "Pomme de terre de saison, idéale pour la conservation."},
                {"PIM-003", "Piment", Product.CAT_LEGUMES, 2.400, 2.150, "kg", "pepper",
                        "Piment vert local, forte demande sur le marché de gros."},
                {"OGN-004", "Oignon", Product.CAT_LEGUMES, 0.950, 1.050, "kg", "onion",
                        "Oignon sec récolté dans la délégation de Regueb."},
                {"DAT-005", "Dattes Deglet Nour", Product.CAT_FRUITS, 8.500, 7.900, "kg", "dates",
                        "Dattes Deglet Nour, catégorie extra, conditionnées localement."},
                {"PSQ-006", "Pastèque", Product.CAT_FRUITS, 1.100, 1.250, "kg", "watermelon",
                        "Pastèque de plein champ, calibre 8 à 12 kg."},
                {"RAI-007", "Raisin", Product.CAT_FRUITS, 4.200, 3.800, "kg", "grapes",
                        "Raisin de table, variété locale, récolte récente."},
                {"FIG-008", "Figue de Barbarie", Product.CAT_FRUITS, 3.000, 3.400, "kg", "fig",
                        "Figues de Barbarie triées, piquants retirés."},
                {"BLE-009", "Blé dur", Product.CAT_CEREALES, 1.150, 1.100, "kg", "wheat",
                        "Blé dur collecté auprès des agriculteurs de la région."},
                {"HUI-010", "Huile d'olive", Product.CAT_FERME, 18.000, 16.500, "L", "oil",
                        "Huile d'olive vierge extra, première pression à froid."},
                {"MIE-011", "Miel de thym", Product.CAT_FERME, 45.000, 45.000, "kg", "honey",
                        "Miel de thym de montagne, production artisanale."},
                {"OEU-012", "Œufs de ferme", Product.CAT_FERME, 0.600, 0.550, "pièce", "eggs",
                        "Œufs frais de poules élevées en plein air."}
        };

        long[] favoriteProductIds = {1, 5, 10};

        for (int i = 0; i < data.length; i++) {
            Object[] row = data[i];
            String ref = (String) row[0];
            String name = (String) row[1];
            String category = (String) row[2];
            double price = (Double) row[3];
            double previousPrice = (Double) row[4];
            String unit = (String) row[5];
            String imageKey = (String) row[6];
            String description = (String) row[7];
            long updatedAt = now - (long) i * 3_600_000L;

            ContentValues values = new ContentValues();
            values.put("ref", ref);
            values.put("name", name);
            values.put("category", category);
            values.put("price", price);
            values.put("previous_price", previousPrice);
            values.put("unit", unit);
            values.put("image_key", imageKey);
            values.put("description", description);
            values.put("updated_at", updatedAt);
            long productId = db.insertOrThrow("products", null, values);

            for (int day = 0; day < 7; day++) {
                double ratio = day / 6.0;
                double value = previousPrice + (price - previousPrice) * ratio;
                if (day > 0 && day < 6) {
                    value += (day % 2 == 0) ? 0.03 : -0.02;
                }
                value = Math.round(value * 1000.0) / 1000.0;
                insertHistory(db, productId, value, now - (long) (6 - day) * 86_400_000L);
            }
        }

        for (long productId : favoriteProductIds) {
            ContentValues favorite = new ContentValues();
            favorite.put("user_id", userId);
            favorite.put("product_id", productId);
            db.insertWithOnConflict("favorites", null, favorite, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private long insertUser(SQLiteDatabase db, String fullName, String email, String phone,
                            String passwordHash, String role) {
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password_hash", passwordHash);
        values.put("role", role);
        values.put("created_at", System.currentTimeMillis());
        return db.insertOrThrow("users", null, values);
    }

    static void insertHistory(SQLiteDatabase db, long productId, double price, long recordedAt) {
        ContentValues values = new ContentValues();
        values.put("product_id", productId);
        values.put("price", price);
        values.put("recorded_at", recordedAt);
        db.insertOrThrow("price_history", null, values);
    }
}
