package tn.isetsb.agrisouk.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.PricePoint;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.ui.widget.PriceChartView;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.ProductImages;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Fiche détaillée d'un produit : variation, historique et favoris.
 */
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private long productId;
    private ProductRepository repository;
    private SessionManager session;
    private Product product;

    private FrameLayout thumb;
    private TextView trend;
    private MaterialButton favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        session = new SessionManager(this);
        repository = new ProductRepository(this);
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, 0);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setNavigationOnClickListener(view -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) {
                shareProduct();
                return true;
            }
            return false;
        });

        thumb = findViewById(R.id.detailThumb);
        trend = findViewById(R.id.tvDetailTrend);
        favoriteButton = findViewById(R.id.btnDetailFavorite);
        favoriteButton.setOnClickListener(view -> toggleFavorite());

        bindProduct();
    }

    private void bindProduct() {
        product = repository.getProduct(productId, session.getUserId());
        if (product == null) {
            Toast.makeText(this, R.string.detail_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvDetailName)).setText(product.getName());
        ((TextView) findViewById(R.id.tvDetailMeta)).setText(
                getString(R.string.ref_label, product.getRef()) + " · " + product.getCategory());
        ((TextView) findViewById(R.id.tvDetailPrice))
                .setText(FormatUtils.price(product.getPrice()));
        ((TextView) findViewById(R.id.tvDetailUnit))
                .setText(getString(R.string.per_unit, product.getUnit()));
        ((TextView) findViewById(R.id.tvDetailUpdated)).setText(
                getString(R.string.detail_updated_format,
                        FormatUtils.dateLong(product.getUpdatedAt())));

        ((ImageView) findViewById(R.id.ivDetailIcon))
                .setImageResource(ProductImages.icon(product.getImageKey()));
        Drawable background = thumb.getBackground();
        if (background != null) {
            background.mutate().setTint(ContextCompat.getColor(this,
                    ProductImages.categoryBackground(product.getCategory())));
        }

        double percent = product.getTrendPercent();
        trend.setText(FormatUtils.change(percent));
        if (percent > 0.05) {
            trend.setBackgroundResource(R.drawable.bg_trend_up);
            trend.setTextColor(ContextCompat.getColor(this, R.color.danger));
        } else if (percent < -0.05) {
            trend.setBackgroundResource(R.drawable.bg_trend_down);
            trend.setTextColor(ContextCompat.getColor(this, R.color.green_800));
        } else {
            trend.setBackgroundResource(R.drawable.bg_trend_stable);
            trend.setTextColor(ContextCompat.getColor(this, R.color.ink_500));
        }

        ((TextView) findViewById(R.id.tvInfoCategory)).setText(product.getCategory());
        ((TextView) findViewById(R.id.tvInfoUnit)).setText(product.getUnit());
        ((TextView) findViewById(R.id.tvInfoRef)).setText(product.getRef());
        ((TextView) findViewById(R.id.tvInfoUpdated))
                .setText(FormatUtils.dateLong(product.getUpdatedAt()));
        ((TextView) findViewById(R.id.tvDetailDescription))
                .setText(product.getDescription());

        List<PricePoint> history = repository.getHistory(productId);
        ((PriceChartView) findViewById(R.id.priceChart)).setPoints(history);
        bindHistoryStats(history);
        updateFavoriteButton();
    }

    private void bindHistoryStats(List<PricePoint> history) {
        TextView minView = findViewById(R.id.tvChartMin);
        TextView averageView = findViewById(R.id.tvChartAverage);
        TextView maxView = findViewById(R.id.tvChartMax);
        if (history.isEmpty()) {
            minView.setText("—");
            averageView.setText("—");
            maxView.setText("—");
            return;
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        for (PricePoint point : history) {
            min = Math.min(min, point.getPrice());
            max = Math.max(max, point.getPrice());
            sum += point.getPrice();
        }
        minView.setText(FormatUtils.priceShort(min));
        maxView.setText(FormatUtils.priceShort(max));
        averageView.setText(FormatUtils.priceShort(sum / history.size()));
    }

    private void updateFavoriteButton() {
        boolean favorite = product.isFavorite();
        favoriteButton.setText(favorite
                ? R.string.action_remove_favorite
                : R.string.action_add_favorite);
        favoriteButton.setIconResource(favorite
                ? R.drawable.ic_favorite
                : R.drawable.ic_favorite_border);
        favoriteButton.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this,
                favorite ? R.color.danger : R.color.green_700)));
    }

    private void toggleFavorite() {
        boolean favorite = repository.toggleFavorite(session.getUserId(), product.getId());
        product.setFavorite(favorite);
        updateFavoriteButton();
        Toast.makeText(this, getString(
                favorite ? R.string.favorite_added : R.string.favorite_removed,
                product.getName()), Toast.LENGTH_SHORT).show();
    }

    private void shareProduct() {
        if (product == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                product.getName(), FormatUtils.price(product.getPrice()), product.getUnit()));
        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser)));
    }
}
