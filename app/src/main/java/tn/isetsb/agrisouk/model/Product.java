package tn.isetsb.agrisouk.model;

/**
 * Produit agricole suivi par la plateforme (prix du marché).
 */
public class Product {

    public static final String CAT_LEGUMES = "Légumes";
    public static final String CAT_FRUITS = "Fruits";
    public static final String CAT_CEREALES = "Céréales";
    public static final String CAT_FERME = "Ferme";

    private long id;
    private String ref;
    private String name;
    private String category;
    private double price;
    private double previousPrice;
    private String unit;
    private String imageKey;
    private String description;
    private long updatedAt;
    private boolean favorite;

    public Product(String ref, String name, String category, double price, double previousPrice,
                   String unit, String imageKey, String description, long updatedAt) {
        this.ref = ref;
        this.name = name;
        this.category = category;
        this.price = price;
        this.previousPrice = previousPrice;
        this.unit = unit;
        this.imageKey = imageKey;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = previousPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * Variation en pourcentage entre le prix précédent et le prix actuel.
     */
    public double getTrendPercent() {
        if (previousPrice <= 0) {
            return 0;
        }
        return (price - previousPrice) / previousPrice * 100.0;
    }
}
