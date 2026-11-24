package tn.isetsb.agrisouk.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.util.ProductImages;

/**
 * Formulaire d'ajout / modification d'un produit, présenté en BottomSheet.
 */
public class ProductFormSheet extends BottomSheetDialogFragment {

    public static final String RESULT_PRODUCT_SAVED = "product_saved";

    private static final String ARG_PRODUCT_ID = "product_id";
    private static final String[] CATEGORIES = {
            Product.CAT_LEGUMES, Product.CAT_FRUITS, Product.CAT_CEREALES, Product.CAT_FERME
    };
    private static final String[] UNITS = {"kg", "L", "pièce", "botte"};

    private ProductRepository repository;
    private Product editing;

    private TextInputLayout tilName;
    private TextInputLayout tilPrice;
    private TextInputEditText etName;
    private TextInputEditText etRef;
    private TextInputEditText etPrice;
    private TextInputEditText etDescription;
    private AutoCompleteTextView autoCategory;
    private AutoCompleteTextView autoUnit;

    public static ProductFormSheet newInstance(long productId) {
        ProductFormSheet sheet = new ProductFormSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_PRODUCT_ID, productId);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_product_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = new ProductRepository(requireContext());
        tilName = view.findViewById(R.id.tilFormName);
        tilPrice = view.findViewById(R.id.tilFormPrice);
        etName = view.findViewById(R.id.etFormName);
        etRef = view.findViewById(R.id.etFormRef);
        etPrice = view.findViewById(R.id.etFormPrice);
        etDescription = view.findViewById(R.id.etFormDescription);
        autoCategory = view.findViewById(R.id.autoCategory);
        autoUnit = view.findViewById(R.id.autoUnit);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_dropdown, CATEGORIES);
        autoCategory.setAdapter(categoryAdapter);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_dropdown, UNITS);
        autoUnit.setAdapter(unitAdapter);

        long productId = requireArguments().getLong(ARG_PRODUCT_ID);
        if (productId > 0) {
            editing = repository.getProduct(productId, 0);
        }

        TextView title = view.findViewById(R.id.tvFormTitle);
        MaterialButton saveButton = view.findViewById(R.id.btnFormSave);
        if (editing != null) {
            title.setText(R.string.admin_edit);
            etName.setText(editing.getName());
            etRef.setText(editing.getRef());
            etPrice.setText(String.valueOf(editing.getPrice()));
            etDescription.setText(editing.getDescription());
            autoCategory.setText(editing.getCategory(), false);
            autoUnit.setText(editing.getUnit(), false);
        } else {
            title.setText(R.string.admin_new);
            autoCategory.setText(CATEGORIES[0], false);
            autoUnit.setText(UNITS[0], false);
        }

        saveButton.setOnClickListener(v -> save());
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view == null || !(view.getParent() instanceof View)) {
            return;
        }
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    private void save() {
        tilName.setError(null);
        tilPrice.setError(null);

        String name = textOf(etName);
        if (name.length() < 2) {
            tilName.setError(getString(R.string.form_error_name));
            return;
        }

        double price;
        try {
            price = Double.parseDouble(textOf(etPrice).replace(',', '.'));
        } catch (NumberFormatException exception) {
            price = 0;
        }
        if (price <= 0) {
            tilPrice.setError(getString(R.string.form_error_price));
            return;
        }

        String category = autoCategory.getText().toString().trim();
        if (!Arrays.asList(CATEGORIES).contains(category)) {
            category = CATEGORIES[0];
        }
        String unit = autoUnit.getText().toString().trim();
        if (unit.isEmpty()) {
            unit = UNITS[0];
        }

        Product product = new Product(
                editing == null ? "" : editing.getRef(),
                name,
                category,
                price,
                editing == null ? price : editing.getPreviousPrice(),
                unit,
                editing == null ? ProductImages.keyForCategory(category) : editing.getImageKey(),
                textOf(etDescription),
                System.currentTimeMillis());
        if (editing != null) {
            product.setId(editing.getId());
        }

        boolean saved = editing == null
                ? repository.insert(product) > 0
                : repository.update(product);
        if (!saved) {
            Toast.makeText(requireContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), R.string.admin_saved, Toast.LENGTH_SHORT).show();
        getParentFragmentManager().setFragmentResult(RESULT_PRODUCT_SAVED, new Bundle());
        dismiss();
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
