package tn.isetsb.agrisouk.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
 * Liste d'administration : chaque ligne propose la modification et la suppression.
 * Comme pour le marché, la ligne ne réactualise que les vues dont la valeur change.
 */
public class AdminProductAdapter extends BaseAdapter {

    public interface Listener {
        void onEdit(Product product);

        void onDelete(Product product);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final Listener listener;
    private List<Product> items = new ArrayList<>();
    private final Map<String, Integer> thumbColors = new HashMap<>();

    public AdminProductAdapter(Context context, List<Product> items, Listener listener) {
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
            convertView = inflater.inflate(R.layout.item_product_admin, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = getItem(position);
        holder.product = product;

        holder.name.setText(product.getName());
        holder.meta.setText(context.getString(R.string.ref_label, product.getRef())
                + " · " + product.getCategory());
        holder.price.setText(FormatUtils.price(product.getPrice())
                + " " + context.getString(R.string.per_unit, product.getUnit()));

        int iconRes = ProductImages.icon(product.getImageKey());
        if (holder.iconRes != iconRes) {
            holder.iconRes = iconRes;
            holder.icon.setImageResource(iconRes);
        }

        int thumbColor = thumbColor(product.getCategory());
        if (holder.thumbColor != thumbColor) {
            holder.thumbColor = thumbColor;
            holder.thumbDrawable.setTint(thumbColor);
        }
        return convertView;
    }

    private int thumbColor(String category) {
        Integer value = thumbColors.get(category);
        if (value == null) {
            value = ContextCompat.getColor(context, ProductImages.categoryBackground(category));
            thumbColors.put(category, value);
        }
        return value;
    }

    private final class ViewHolder {
        private final View itemView;
        private final FrameLayout thumb;
        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView price;
        private final ImageButton edit;
        private final ImageButton delete;
        private final Drawable thumbDrawable;

        private Product product;
        private int iconRes;
        private int thumbColor;

        private ViewHolder(View view) {
            itemView = view;
            thumb = view.findViewById(R.id.adminThumb);
            icon = view.findViewById(R.id.ivAdminIcon);
            name = view.findViewById(R.id.tvAdminName);
            meta = view.findViewById(R.id.tvAdminMeta);
            price = view.findViewById(R.id.tvAdminPrice);
            edit = view.findViewById(R.id.btnAdminEdit);
            delete = view.findViewById(R.id.btnAdminDelete);

            thumbDrawable = thumb.getBackground().mutate();

            itemView.setOnClickListener(v -> {
                if (product != null) {
                    listener.onEdit(product);
                }
            });
            edit.setOnClickListener(v -> {
                if (product != null) {
                    listener.onEdit(product);
                }
            });
            delete.setOnClickListener(v -> {
                if (product != null) {
                    listener.onDelete(product);
                }
            });
        }
    }
}
