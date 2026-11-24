package tn.isetsb.agrisouk.util;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.Product;

/**
 * Association entre les clés stockées en base et les ressources graphiques.
 */
public final class ProductImages {

    private ProductImages() {
    }

    public static int icon(String key) {
        if (key == null) {
            return R.drawable.ic_leaf;
        }
        switch (key) {
            case "tomato":
                return R.drawable.ic_tomato;
            case "potato":
                return R.drawable.ic_potato;
            case "pepper":
                return R.drawable.ic_pepper;
            case "onion":
                return R.drawable.ic_onion;
            case "dates":
                return R.drawable.ic_dates;
            case "watermelon":
                return R.drawable.ic_watermelon;
            case "grapes":
                return R.drawable.ic_grapes;
            case "fig":
                return R.drawable.ic_fig;
            case "wheat":
                return R.drawable.ic_wheat;
            case "oil":
                return R.drawable.ic_oil;
            case "honey":
                return R.drawable.ic_honey;
            case "eggs":
                return R.drawable.ic_eggs;
            default:
                return R.drawable.ic_leaf;
        }
    }

    public static int categoryIcon(String category) {
        if (Product.CAT_LEGUMES.equals(category)) {
            return R.drawable.ic_tomato;
        } else if (Product.CAT_FRUITS.equals(category)) {
            return R.drawable.ic_grapes;
        } else if (Product.CAT_CEREALES.equals(category)) {
            return R.drawable.ic_wheat;
        } else if (Product.CAT_FERME.equals(category)) {
            return R.drawable.ic_honey;
        }
        return R.drawable.ic_leaf;
    }

    public static int categoryColor(String category) {
        if (Product.CAT_LEGUMES.equals(category)) {
            return R.color.cat_legume;
        } else if (Product.CAT_FRUITS.equals(category)) {
            return R.color.cat_fruit;
        } else if (Product.CAT_CEREALES.equals(category)) {
            return R.color.cat_cereale;
        } else if (Product.CAT_FERME.equals(category)) {
            return R.color.cat_ferme;
        }
        return R.color.green_700;
    }

    public static int categoryBackground(String category) {
        if (Product.CAT_LEGUMES.equals(category)) {
            return R.color.cat_legume_bg;
        } else if (Product.CAT_FRUITS.equals(category)) {
            return R.color.cat_fruit_bg;
        } else if (Product.CAT_CEREALES.equals(category)) {
            return R.color.cat_cereale_bg;
        } else if (Product.CAT_FERME.equals(category)) {
            return R.color.cat_ferme_bg;
        }
        return R.color.green_50;
    }

    public static String keyForCategory(String category) {
        if (Product.CAT_LEGUMES.equals(category)) {
            return "tomato";
        } else if (Product.CAT_FRUITS.equals(category)) {
            return "grapes";
        } else if (Product.CAT_CEREALES.equals(category)) {
            return "wheat";
        } else if (Product.CAT_FERME.equals(category)) {
            return "honey";
        }
        return "leaf";
    }
}
