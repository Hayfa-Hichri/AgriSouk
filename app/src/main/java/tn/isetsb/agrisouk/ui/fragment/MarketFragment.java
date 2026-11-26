package tn.isetsb.agrisouk.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.ui.MainActivity;
import tn.isetsb.agrisouk.ui.adapter.ProductAdapter;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Marché : recherche, filtre par catégorie, tri et suivi des favoris.
 */
public class MarketFragment extends Fragment implements ProductAdapter.Listener {

    private static final int SORT_NAME = 0;
    private static final int SORT_PRICE_ASC = 1;
    private static final int SORT_PRICE_DESC = 2;
    private static final int SORT_RECENT = 3;

    private ProductRepository repository;
    private SessionManager session;
    private ProductAdapter adapter;

    private List<Product> allProducts = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private String selectedCategory;
    private int sortMode = SORT_NAME;

    private TextInputEditText etSearch;
    private Spinner spinnerCategory;
    private Spinner spinnerSort;
    private TextView tvMarketCount;
    private ListView listProducts;
    private SwipeRefreshLayout swipeMarket;
    private int categorySelection = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_market, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = new ProductRepository(requireContext());
        session = new SessionManager(requireContext());
        MainActivity main = (MainActivity) requireActivity();

        tvMarketCount = view.findViewById(R.id.tvMarketCount);
        etSearch = view.findViewById(R.id.etSearch);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        listProducts = view.findViewById(R.id.listProducts);
        swipeMarket = view.findViewById(R.id.swipeMarket);

        adapter = new ProductAdapter(requireContext(), new ArrayList<>(), this);
        listProducts.setAdapter(adapter);
        listProducts.setEmptyView(view.findViewById(R.id.emptyState));

        FloatingActionButton fabManage = view.findViewById(R.id.fabManage);
        if (session.isAdmin()) {
            fabManage.setVisibility(View.VISIBLE);
            fabManage.setOnClickListener(v -> main.openAdmin());
        }

        view.findViewById(R.id.btnResetFilters).setOnClickListener(v -> resetFilters());
        swipeMarket.setColorSchemeResources(R.color.green_700);
        swipeMarket.setOnRefreshListener(this::loadProducts);

        setupCategorySpinner();
        setupSortSpinner();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters(true);
            }
        });

        loadProducts();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadProducts();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && repository != null) {
            loadProducts();
        }
    }

    /**
     * Appelée par MainActivity lorsqu'une catégorie est choisie sur l'accueil.
     */
    public void setCategory(String category) {
        selectedCategory = category;
        if (spinnerCategory != null && category != null) {
            int position = categories.indexOf(category);
            if (position >= 0) {
                categorySelection = position + 1;
                spinnerCategory.setSelection(categorySelection);
            }
        }
        applyFilters(true);
    }

    private void setupCategorySpinner() {
        categories = repository.getCategories();
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.filter_all_categories));
        labels.addAll(categories);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner, labels);
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setSelection(categorySelection);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categorySelection = position;
                selectedCategory = position == 0 ? null : categories.get(position - 1);
                applyFilters(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSortSpinner() {
        String[] labels = {
                getString(R.string.sort_name),
                getString(R.string.sort_price_asc),
                getString(R.string.sort_price_desc),
                getString(R.string.sort_recent)
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner, labels);
        sortAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(sortMode);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortMode = position;
                applyFilters(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadProducts() {
        allProducts = repository.getProducts(session.getUserId());
        applyFilters(false);
        swipeMarket.setRefreshing(false);
    }

    private void applyFilters(boolean scrollToTop) {
        if (adapter == null || etSearch == null) {
            return;
        }
        String query = etSearch.getText() == null
                ? ""
                : etSearch.getText().toString().trim().toLowerCase(Locale.FRENCH);

        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            boolean matchesCategory = selectedCategory == null
                    || selectedCategory.equals(product.getCategory());
            boolean matchesQuery = query.isEmpty()
                    || product.getName().toLowerCase(Locale.FRENCH).contains(query)
                    || product.getRef().toLowerCase(Locale.FRENCH).contains(query);
            if (matchesCategory && matchesQuery) {
                filtered.add(product);
            }
        }

        if (sortMode == SORT_PRICE_ASC) {
            Collections.sort(filtered, (first, second) ->
                    Double.compare(first.getPrice(), second.getPrice()));
        } else if (sortMode == SORT_PRICE_DESC) {
            Collections.sort(filtered, (first, second) ->
                    Double.compare(second.getPrice(), first.getPrice()));
        } else if (sortMode == SORT_RECENT) {
            Collections.sort(filtered, (first, second) ->
                    Long.compare(second.getUpdatedAt(), first.getUpdatedAt()));
        } else {
            Collections.sort(filtered, (first, second) ->
                    first.getName().compareToIgnoreCase(second.getName()));
        }

        adapter.setItems(filtered);
        if (scrollToTop) {
            listProducts.setSelection(0);
        }
        tvMarketCount.setText(filtered.size() == 1
                ? getString(R.string.product_count_one)
                : getString(R.string.products_count, filtered.size()));
    }

    private void resetFilters() {
        etSearch.setText("");
        categorySelection = 0;
        spinnerCategory.setSelection(0);
        spinnerSort.setSelection(SORT_NAME);
        selectedCategory = null;
        applyFilters(true);
    }

    @Override
    public void onProductClick(Product product) {
        ((MainActivity) requireActivity()).openProduct(product);
    }

    @Override
    public void onFavoriteClick(Product product) {
        boolean favorite = repository.toggleFavorite(session.getUserId(), product.getId());
        product.setFavorite(favorite);
        adapter.notifyDataSetChanged();
        Context context = requireContext();
        Toast.makeText(context, getString(
                favorite ? R.string.favorite_added : R.string.favorite_removed,
                product.getName()), Toast.LENGTH_SHORT).show();
    }
}
