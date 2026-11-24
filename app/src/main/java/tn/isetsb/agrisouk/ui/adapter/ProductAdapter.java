package tn.isetsb.agrisouk.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
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
import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.ProductImages;

/**
 * ListView personnalisée : BaseAdapter + ViewHolder + recyclage de convertView.
 */
public class ProductAdapter extends BaseAdapter {

    public interface Listener {
        void onProductClick(Product product);

        void onFavoriteClick(Product product);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final Listener listener;
    private List<Product> items = new ArrayList<>();

    public ProductAdapter(Context context, List<Product> items, Listener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.items = items == null ? new ArrayList<>() : items;
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
        holder.name.setText(product.getName());

        String ref = context.getString(R.string.ref_label, product.getRef());
        String meta = ref + " · " + product.getCategory();
        SpannableString metaSpan = new SpannableString(meta);
        metaSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,
                        ProductImages.categoryColor(product.getCategory()))),
                ref.length() + 3, meta.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.meta.setText(metaSpan);

        holder.price.setText(FormatUtils.price(product.getPrice()));
        holder.unit.setText(context.getString(R.string.per_unit, product.getUnit()));
        holder.icon.setImageResource(ProductImages.icon(product.getImageKey()));

        Drawable thumb = holder.thumb.getBackground();
        if (thumb != null) {
            thumb.mutate().setTint(ContextCompat.getColor(context,
                    ProductImages.categoryBackground(product.getCategory())));
        }

        double percent = product.getTrendPercent();
        holder.trend.setText(FormatUtils.change(percent));
        if (percent > 0.05) {
            holder.trend.setBackgroundResource(R.drawable.bg_trend_up);
            holder.trend.setTextColor(ContextCompat.getColor(context, R.color.danger));
        } else if (percent < -0.05) {
            holder.trend.setBackgroundResource(R.drawable.bg_trend_down);
            holder.trend.setTextColor(ContextCompat.getColor(context, R.color.green_800));
        } else {
            holder.trend.setBackgroundResource(R.drawable.bg_trend_stable);
            holder.trend.setTextColor(ContextCompat.getColor(context, R.color.ink_500));
        }

        boolean favorite = product.isFavorite();
        holder.favorite.setImageResource(favorite
                ? R.drawable.ic_favorite
                : R.drawable.ic_favorite_border);
        holder.favorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                favorite ? R.color.danger : R.color.ink_300)));

        holder.itemView.setOnClickListener(view -> listener.onProductClick(product));
        holder.favorite.setOnClickListener(view -> listener.onFavoriteClick(product));
        return convertView;
    }

    private static final class ViewHolder {
        private final View itemView;
        private final FrameLayout thumb;
        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView price;
        private final TextView unit;
        private final TextView trend;
        private final ImageButton favorite;

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
        }
    }
}
