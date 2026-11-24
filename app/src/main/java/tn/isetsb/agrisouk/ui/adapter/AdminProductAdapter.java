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
import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.ProductImages;

/**
 * Liste d'administration : chaque ligne propose la modification et la suppression.
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
        holder.name.setText(product.getName());
        holder.meta.setText(context.getString(R.string.ref_label, product.getRef())
                + " · " + product.getCategory());
        holder.price.setText(FormatUtils.price(product.getPrice())
                + " " + context.getString(R.string.per_unit, product.getUnit()));
        holder.icon.setImageResource(ProductImages.icon(product.getImageKey()));

        Drawable thumb = holder.thumb.getBackground();
        if (thumb != null) {
            thumb.mutate().setTint(ContextCompat.getColor(context,
                    ProductImages.categoryBackground(product.getCategory())));
        }

        holder.itemView.setOnClickListener(view -> listener.onEdit(product));
        holder.edit.setOnClickListener(view -> listener.onEdit(product));
        holder.delete.setOnClickListener(view -> listener.onDelete(product));
        return convertView;
    }

    private static final class ViewHolder {
        private final View itemView;
        private final FrameLayout thumb;
        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView price;
        private final ImageButton edit;
        private final ImageButton delete;

        private ViewHolder(View view) {
            itemView = view;
            thumb = view.findViewById(R.id.adminThumb);
            icon = view.findViewById(R.id.ivAdminIcon);
            name = view.findViewById(R.id.tvAdminName);
            meta = view.findViewById(R.id.tvAdminMeta);
            price = view.findViewById(R.id.tvAdminPrice);
            edit = view.findViewById(R.id.btnAdminEdit);
            delete = view.findViewById(R.id.btnAdminDelete);
        }
    }
}
