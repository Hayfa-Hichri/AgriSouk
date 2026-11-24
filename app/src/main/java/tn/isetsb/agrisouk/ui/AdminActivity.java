package tn.isetsb.agrisouk.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.ui.adapter.AdminProductAdapter;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Espace administrateur : ajout, modification et suppression des produits.
 */
public class AdminActivity extends AppCompatActivity implements AdminProductAdapter.Listener {

    private SessionManager session;
    private ProductRepository repository;
    private AdminProductAdapter adapter;
    private TextInputEditText etSearch;
    private TextView tvCount;
    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isAdmin()) {
            Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_admin);

        repository = new ProductRepository(this);
        ((MaterialToolbar) findViewById(R.id.toolbarAdmin))
                .setNavigationOnClickListener(view -> finish());

        etSearch = findViewById(R.id.etAdminSearch);
        tvCount = findViewById(R.id.tvAdminCount);
        ListView listAdmin = findViewById(R.id.listAdmin);
        listAdmin.setEmptyView(findViewById(R.id.emptyAdmin));

        adapter = new AdminProductAdapter(this, new ArrayList<>(), this);
        listAdmin.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> openForm(0));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });

        getSupportFragmentManager().setFragmentResultListener(
                ProductFormSheet.RESULT_PRODUCT_SAVED, this,
                (requestKey, result) -> loadProducts());

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadProducts();
        }
    }

    private void loadProducts() {
        products = repository.getProducts(session.getUserId());
        applyFilter();
    }

    private void applyFilter() {
        String query = etSearch.getText() == null
                ? ""
                : etSearch.getText().toString().trim().toLowerCase(Locale.FRENCH);

        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (query.isEmpty()
                    || product.getName().toLowerCase(Locale.FRENCH).contains(query)
                    || product.getRef().toLowerCase(Locale.FRENCH).contains(query)) {
                filtered.add(product);
            }
        }
        adapter.setItems(filtered);
        tvCount.setText(filtered.size() == 1
                ? getString(R.string.admin_count_one)
                : getString(R.string.admin_count, filtered.size()));
    }

    private void openForm(long productId) {
        ProductFormSheet.newInstance(productId).show(getSupportFragmentManager(), "product_form");
    }

    @Override
    public void onEdit(Product product) {
        openForm(product.getId());
    }

    @Override
    public void onDelete(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.admin_delete_title)
                .setMessage(getString(R.string.admin_delete_message, product.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    repository.delete(product.getId());
                    Toast.makeText(this, R.string.admin_deleted, Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .show();
    }
}
