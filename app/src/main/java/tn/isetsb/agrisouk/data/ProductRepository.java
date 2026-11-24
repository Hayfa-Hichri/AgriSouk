package tn.isetsb.agrisouk.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tn.isetsb.agrisouk.model.PricePoint;
import tn.isetsb.agrisouk.model.Product;

/**
 * Accès aux produits, favoris, historique et statistiques du marché.
 */
public class ProductRepository {

    private static final String SELECT_PRODUCTS =
            "SELECT p.*, CASE WHEN f.product_id IS NULL THEN 0 ELSE 1 END AS favorite " +
                    "FROM products p LEFT JOIN favorites f " +
                    "ON f.product_id = p.id AND f.user_id = ? ";

    private static final String CATEGORY_ORDER =
            "CASE p.category WHEN 'Légumes' THEN 1 WHEN 'Fruits' THEN 2 " +
                    "WHEN 'Céréales' THEN 3 ELSE 4 END";

    private final DatabaseHelper helper;

    public ProductRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public List<Product> getProducts(long userId) {
        String sql = SELECT_PRODUCTS + "ORDER BY " + CATEGORY_ORDER + ", p.name COLLATE NOCASE";
        return queryProducts(sql, new String[]{String.valueOf(userId)});
    }

    public List<Product> getFavorites(long userId) {
        String sql = SELECT_PRODUCTS + "WHERE f.product_id IS NOT NULL ORDER BY p.name COLLATE NOCASE";
        return queryProducts(sql, new String[]{String.valueOf(userId)});
    }

    public Product getProduct(long productId, long userId) {
        String sql = SELECT_PRODUCTS + "WHERE p.id = ?";
        List<Product> products = queryProducts(sql,
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        return products.isEmpty() ? null : products.get(0);
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY CASE category " +
                "WHEN 'Légumes' THEN 1 WHEN 'Fruits' THEN 2 WHEN 'Céréales' THEN 3 ELSE 4 END";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(0));
            }
        }
        return categories;
    }

    public int countByCategory(String category) {
        return count("SELECT COUNT(*) FROM products WHERE category = ?",
                new String[]{category});
    }

    public int countProducts() {
        return count("SELECT COUNT(*) FROM products", null);
    }

    public int countCategories() {
        return count("SELECT COUNT(DISTINCT category) FROM products", null);
    }

    public int countFavorites(long userId) {
        return count("SELECT COUNT(*) FROM favorites WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    public int countAllFavorites() {
        return count("SELECT COUNT(*) FROM favorites", null);
    }

    public double averagePrice() {
        try (Cursor cursor = helper.getReadableDatabase()
                .rawQuery("SELECT AVG(price) FROM products", null)) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        }
        return 0;
    }

    public boolean isFavorite(long userId, long productId) {
        return count("SELECT COUNT(*) FROM favorites WHERE user_id = ? AND product_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}) > 0;
    }

    /**
     * @return le nouvel état du favori (true = ajouté).
     */
    public boolean toggleFavorite(long userId, long productId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean favorite = isFavorite(userId, productId);
        if (favorite) {
            db.delete("favorites", "user_id = ? AND product_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
        } else {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("product_id", productId);
            db.insertWithOnConflict("favorites", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return !favorite;
    }

    public List<PricePoint> getHistory(long productId) {
        List<PricePoint> points = new ArrayList<>();
        String sql = "SELECT price, recorded_at FROM price_history WHERE product_id = ? " +
                "ORDER BY recorded_at ASC";
        try (Cursor cursor = helper.getReadableDatabase()
                .rawQuery(sql, new String[]{String.valueOf(productId)})) {
            while (cursor.moveToNext()) {
                points.add(new PricePoint(cursor.getDouble(0), cursor.getLong(1)));
            }
        }
        return points;
    }

    public long insert(Product product) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (product.getRef() == null || product.getRef().trim().isEmpty()) {
            product.setRef(generateRef(product.getName()));
        }
        long now = System.currentTimeMillis();
        product.setUpdatedAt(now);
        product.setPreviousPrice(product.getPrice());

        db.beginTransaction();
        try {
            ContentValues values = productValues(product);
            long id = db.insertOrThrow("products", null, values);
            DatabaseHelper.insertHistory(db, id, product.getPrice(), now);
            db.setTransactionSuccessful();
            product.setId(id);
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public boolean update(Product product) {
        SQLiteDatabase db = helper.getWritableDatabase();
        double oldPrice = currentPrice(db, product.getId());
        long now = System.currentTimeMillis();

        product.setPreviousPrice(oldPrice);
        product.setUpdatedAt(now);

        db.beginTransaction();
        try {
            int updated = db.update("products", productValues(product), "id = ?",
                    new String[]{String.valueOf(product.getId())});
            if (Math.abs(oldPrice - product.getPrice()) > 0.0001) {
                DatabaseHelper.insertHistory(db, product.getId(), product.getPrice(), now);
            }
            db.setTransactionSuccessful();
            return updated > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean delete(long productId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] id = {String.valueOf(productId)};
        db.beginTransaction();
        try {
            db.delete("favorites", "product_id = ?", id);
            db.delete("price_history", "product_id = ?", id);
            int deleted = db.delete("products", "id = ?", id);
            db.setTransactionSuccessful();
            return deleted > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean refExists(String ref) {
        return count("SELECT COUNT(*) FROM products WHERE ref = ? COLLATE NOCASE",
                new String[]{ref}) > 0;
    }

    public String generateRef(String name) {
        String letters = name == null ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.length() < 3) {
            letters = (letters + "AGR").substring(0, 3);
        }
        String prefix = letters.substring(0, 3);
        for (int index = 1; index < 1000; index++) {
            String candidate = String.format(Locale.ROOT, "%s-%03d", prefix, index);
            if (!refExists(candidate)) {
                return candidate;
            }
        }
        return "AGR-" + (System.currentTimeMillis() % 1000);
    }

    private List<Product> queryProducts(String sql, String[] args) {
        List<Product> products = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                products.add(mapProduct(cursor));
            }
        }
        return products;
    }

    private Product mapProduct(Cursor cursor) {
        Product product = new Product(
                cursor.getString(cursor.getColumnIndexOrThrow("ref")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("previous_price")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                cursor.getString(cursor.getColumnIndexOrThrow("image_key")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        product.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        int favoriteIndex = cursor.getColumnIndex("favorite");
        if (favoriteIndex >= 0) {
            product.setFavorite(cursor.getInt(favoriteIndex) == 1);
        }
        return product;
    }

    private int count(String sql, String[] args) {
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql, args)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private double currentPrice(SQLiteDatabase db, long productId) {
        try (Cursor cursor = db.rawQuery("SELECT price FROM products WHERE id = ?",
                new String[]{String.valueOf(productId)})) {
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        }
    }

    private ContentValues productValues(Product product) {
        ContentValues values = new ContentValues();
        values.put("ref", product.getRef());
        values.put("name", product.getName());
        values.put("category", product.getCategory());
        values.put("price", product.getPrice());
        values.put("previous_price", product.getPreviousPrice());
        values.put("unit", product.getUnit());
        values.put("image_key", product.getImageKey());
        values.put("description", product.getDescription());
        values.put("updated_at", product.getUpdatedAt());
        return values;
    }
}
