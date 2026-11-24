package tn.isetsb.agrisouk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.ui.MainActivity;
import tn.isetsb.agrisouk.ui.adapter.ProductAdapter;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Feuille des produits suivis, accessible depuis l'accueil ou le profil.
 */
public class FavoritesDialog extends BottomSheetDialogFragment implements ProductAdapter.Listener {

    private ProductRepository repository;
    private SessionManager session;
    private ProductAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = new ProductRepository(requireContext());
        session = new SessionManager(requireContext());
        emptyView = view.findViewById(R.id.tvFavoritesEmpty);

        ListView list = view.findViewById(R.id.listFavorites);
        adapter = new ProductAdapter(requireContext(), new ArrayList<>(), this);
        list.setAdapter(adapter);
        reload();
    }

    private void reload() {
        adapter.setItems(repository.getFavorites(session.getUserId()));
        emptyView.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProductClick(Product product) {
        dismiss();
        ((MainActivity) requireActivity()).openProduct(product);
    }

    @Override
    public void onFavoriteClick(Product product) {
        boolean favorite = repository.toggleFavorite(session.getUserId(), product.getId());
        reload();
        Toast.makeText(requireContext(), getString(
                favorite ? R.string.favorite_added : R.string.favorite_removed,
                product.getName()), Toast.LENGTH_SHORT).show();
    }
}
