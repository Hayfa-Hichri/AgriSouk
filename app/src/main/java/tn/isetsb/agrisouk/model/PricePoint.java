package tn.isetsb.agrisouk.model;

/**
 * Point d'historique de prix utilisé par le graphique.
 */
public class PricePoint {

    private final double price;
    private final long recordedAt;

    public PricePoint(double price, long recordedAt) {
        this.price = price;
        this.recordedAt = recordedAt;
    }

    public double getPrice() {
        return price;
    }

    public long getRecordedAt() {
        return recordedAt;
    }
}
