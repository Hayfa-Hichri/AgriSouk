package tn.isetsb.agrisouk.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.ProductImages;

/**
 * ListView personnalisée : BaseAdapter + ViewHolder + recyclage de convertView.
 *
 * <p>Pour que le défilement reste fluide, chaque ligne mémorise l'état de ses vues
 * (icône, teinte, badge de tendance, favori) : on ne modifie une vue que lorsque sa
 * valeur change vraiment, au lieu de recharger un drawable à chaque liaison.</p>
 */
public class ProductAdapter extends BaseAdapter {

    public interface Listener {
        void onProductClick(Product product);

        void onFavoriteClick(Product product);
    }

    private static final int TREND_UNSET = -1;
    private static final int TREND_STABLE = 0;
    private static final int TREND_UP = 1;
    private static final int TREND_DOWN = 2;

    private final Context context;
    private final LayoutInflater inflater;
    private final Listener listener;
    private List<Product> items = new ArrayList<>();

    private final Map<String, Integer> labelColors = new HashMap<>();
    private final Map<String, Integer> thumbColors = new HashMap<>();
    private final Map<String, ForegroundColorSpan> categorySpans = new HashMap<>();

    private final ColorStateList favoriteOn;
    private final ColorStateList favoriteOff;
    private final int trendUpText;
    private final int trendUpBackground;
    private final int trendDownText;
    private final int trendDownBackground;
    private final int trendStableText;
    private final int trendStableBackground;

    public ProductAdapter(Context context, List<Product> items, Listener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.items = items == null ? new ArrayList<>() : items;

        favoriteOn = ColorStateList.valueOf(color(R.color.danger));
        favoriteOff = ColorStateList.valueOf(color(R.color.ink_300));
        trendUpText = color(R.color.danger);
        trendUpBackground = color(R.color.danger_50);
        trendDownText = color(R.color.green_800);
        trendDownBackground = color(R.color.green_50);
        trendStableText = color(R.color.ink_500);
        trendStableBackground = color(R.color.surface_alt);
    }

    public void setItems(List<Product> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Product getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = getItem(position);
        holder.product = product;
        holder.name.setText(product.getName());
        holder.meta.setText(buildMeta(product));
        holder.price.setText(FormatUtils.price(product.getPrice()));
        holder.unit.setText(context.getString(R.string.per_unit, product.getUnit()));

        int iconRes = ProductImages.icon(product.getImageKey());
        if (holder.iconRes != iconRes) {
            holder.iconRes = iconRes;
            holder.icon.setImageResource(iconRes);
        }

        int thumbColor = cachedColor(thumbColors, product.getCategory(),
                ProductImages.categoryBackground(product.getCategory()));
        if (holder.thumbColor != thumbColor) {
            holder.thumbColor = thumbColor;
            holder.thumbDrawable.setTint(thumbColor);
        }

        double percent = product.getTrendPercent();
        if (percent > 0.05) {
            bindTrend(holder, TREND_UP, FormatUtils.change(percent));
        } else if (percent < -0.05) {
            bindTrend(holder, TREND_DOWN, FormatUtils.change(percent));
        } else {
            bindTrend(holder, TREND_STABLE, FormatUtils.change(percent));
        }

        boolean favorite = product.isFavorite();
        if (holder.favoriteState != (favorite ? 1 : 0)) {
            holder.favoriteState = favorite ? 1 : 0;
            holder.favorite.setImageResource(favorite
                    ? R.drawable.ic_favorite
                    : R.drawable.ic_favorite_border);
            holder.favorite.setImageTintList(favorite ? favoriteOn : favoriteOff);
        }
        return convertView;
    }

    private void bindTrend(ViewHolder holder, int style, String text) {
        holder.trend.setText(text);
        if (holder.trendStyle == style) {
            return;
        }
        holder.trendStyle = style;
        if (style == TREND_UP) {
            holder.trendBackground.setColor(trendUpBackground);
            holder.trend.setTextColor(trendUpText);
        } else if (style == TREND_DOWN) {
            holder.trendBackground.setColor(trendDownBackground);
            holder.trend.setTextColor(trendDownText);
        } else {
            holder.trendBackground.setColor(trendStableBackground);
            holder.trend.setTextColor(trendStableText);
        }
    }

    private CharSequence buildMeta(Product product) {
        String ref = context.getString(R.string.ref_label, product.getRef());
        String meta = ref + " · " + product.getCategory();
        SpannableString span = new SpannableString(meta);
        span.setSpan(categorySpan(product.getCategory()),
                ref.length() + 3, meta.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private ForegroundColorSpan categorySpan(String category) {
        ForegroundColorSpan span = categorySpans.get(category);
        if (span == null) {
            span = new ForegroundColorSpan(cachedColor(labelColors, category,
                    ProductImages.categoryColor(category)));
            categorySpans.put(category, span);
        }
        return span;
    }

    private int cachedColor(Map<String, Integer> cache, String key, int colorRes) {
        Integer value = cache.get(key);
        if (value == null) {
            value = color(colorRes);
            cache.put(key, value);
        }
        return value;
    }

    private int color(int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }

    private final class ViewHolder {
        private final View itemView;
        private final FrameLayout thumb;
        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView price;
        private final TextView unit;
        private final TextView trend;
        private final ImageButton favorite;
        private final GradientDrawable trendBackground;
        private final Drawable thumbDrawable;

        private Product product;
        private int iconRes;
        private int thumbColor;
        private int trendStyle = TREND_UNSET;
        private int favoriteState = -1;

        private ViewHolder(View view) {
            itemView = view;
            thumb = view.findViewById(R.id.productThumb);
            icon = view.findViewById(R.id.ivProductIcon);
            name = view.findViewById(R.id.tvProductName);
            meta = view.findViewById(R.id.tvProductMeta);
            price = view.findViewById(R.id.tvProductPrice);
            unit = view.findViewById(R.id.tvProductUnit);
            trend = view.findViewById(R.id.tvTrend);
            favorite = view.findViewById(R.id.btnFavorite);

            thumbDrawable = thumb.getBackground().mutate();

            trendBackground = new GradientDrawable();
            trendBackground.setShape(GradientDrawable.RECTANGLE);
            trendBackground.setCornerRadius(8f * view.getResources().getDisplayMetrics().density);
            trend.setBackground(trendBackground);

            itemView.setOnClickListener(v -> {
                if (product != null) {
                    listener.onProductClick(product);
                }
            });
            favorite.setOnClickListener(v -> {
                if (product != null) {
                    listener.onFavoriteClick(product);
                }
            });
        }
    }
}
