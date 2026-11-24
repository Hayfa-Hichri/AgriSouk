package tn.isetsb.agrisouk.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.util.ProductImages;

/**
 * GridView des catégories : chaque cellule affiche l'icône et le nombre de produits.
 */
public class CategoryAdapter extends BaseAdapter {

    public static class Item {
        private final String category;
        private final int count;

        public Item(String category, int count) {
            this.category = category;
            this.count = count;
        }

        public String getCategory() {
            return category;
        }

        public int getCount() {
            return count;
        }
    }

    public interface Listener {
        void onCategoryClick(String category);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final Listener listener;
    private List<Item> items = new ArrayList<>();

    public CategoryAdapter(Context context, List<Item> items, Listener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.items = items == null ? new ArrayList<>() : items;
    }

    public void setItems(List<Item> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.name.setText(item.getCategory());
        holder.count.setText(item.getCount() == 1
                ? context.getString(R.string.product_count_one)
                : context.getString(R.string.products_count, item.getCount()));
        holder.icon.setImageResource(ProductImages.categoryIcon(item.getCategory()));

        Drawable thumb = holder.thumb.getBackground();
        if (thumb != null) {
            thumb.mutate().setTint(ContextCompat.getColor(context,
                    ProductImages.categoryBackground(item.getCategory())));
        }

        convertView.setOnClickListener(view -> listener.onCategoryClick(item.getCategory()));
        return convertView;
    }

    private static final class ViewHolder {
        private final FrameLayout thumb;
        private final ImageView icon;
        private final TextView name;
        private final TextView count;

        private ViewHolder(View view) {
            thumb = view.findViewById(R.id.categoryThumb);
            icon = view.findViewById(R.id.ivCategoryIcon);
            name = view.findViewById(R.id.tvCategoryName);
            count = view.findViewById(R.id.tvCategoryCount);
        }
    }
}
