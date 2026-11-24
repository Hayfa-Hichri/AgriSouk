package tn.isetsb.agrisouk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.ui.MainActivity;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Profil : identité, rôle et accès aux fonctions du compte.
 */
public class ProfileFragment extends Fragment {

    private SessionManager session;
    private ProductRepository repository;
    private TextView tvProfileStatMarket;
    private TextView tvProfileStatFavorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        repository = new ProductRepository(requireContext());
        MainActivity main = (MainActivity) requireActivity();

        ((TextView) view.findViewById(R.id.tvAvatarInitials)).setText(session.getInitials());
        ((TextView) view.findViewById(R.id.tvProfileName)).setText(session.getFullName());
        ((TextView) view.findViewById(R.id.tvProfileEmail)).setText(session.getEmail());
        ((TextView) view.findViewById(R.id.tvProfileVersion))
                .setText(R.string.splash_version);

        boolean admin = session.isAdmin();
        TextView role = view.findViewById(R.id.tvProfileRole);
        role.setText(admin ? R.string.role_admin : R.string.role_user);
        role.setBackgroundResource(admin ? R.drawable.bg_chip_admin : R.drawable.bg_chip_user);
        role.setTextColor(ContextCompat.getColor(requireContext(),
                admin ? R.color.amber_700 : R.color.green_800));

        tvProfileStatMarket = view.findViewById(R.id.tvProfileStatMarket);
        tvProfileStatFavorites = view.findViewById(R.id.tvProfileStatFavorites);

        View rowAdmin = bindRow(view, R.id.rowAdmin, R.drawable.ic_dashboard,
                R.string.menu_admin, R.string.menu_admin_sub,
                v -> main.openAdmin());
        rowAdmin.setVisibility(admin ? View.VISIBLE : View.GONE);

        bindRow(view, R.id.rowFavorites, R.drawable.ic_favorite,
                R.string.menu_favorites, R.string.menu_favorites_sub,
                v -> main.showFavorites());
        bindRow(view, R.id.rowBulletin, R.drawable.ic_bulletin,
                R.string.menu_bulletin, R.string.menu_bulletin_sub,
                v -> main.openWebView(false));
        bindRow(view, R.id.rowAbout, R.drawable.ic_info,
                R.string.menu_about, R.string.menu_about_sub,
                v -> showAbout());
        View rowLogout = bindRow(view, R.id.rowLogout, R.drawable.ic_logout,
                R.string.menu_logout, R.string.menu_logout_sub,
                v -> main.confirmLogout());
        ((TextView) rowLogout.findViewById(R.id.tvRowTitle))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.danger));
        ((ImageView) rowLogout.findViewById(R.id.ivRowIcon))
                .setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.danger));
        rowLogout.findViewById(R.id.ivRowIcon)
                .setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.danger_50));

        loadStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadStats();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && repository != null) {
            loadStats();
        }
    }

    private View bindRow(View root, int rowId, int iconRes, int titleRes, int subRes,
                         View.OnClickListener listener) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.ivRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.tvRowTitle)).setText(titleRes);
        ((TextView) row.findViewById(R.id.tvRowSub)).setText(subRes);
        row.setOnClickListener(listener);
        return row;
    }

    private void loadStats() {
        tvProfileStatMarket.setText(String.valueOf(repository.countProducts()));
        tvProfileStatFavorites.setText(String.valueOf(repository.countFavorites(session.getUserId())));
    }

    private void showAbout() {
        String message = getString(R.string.about_body)
                + "\n\n" + getString(R.string.about_api)
                + "\n" + getString(R.string.about_storage)
                + "\n" + getString(R.string.about_views)
                + "\n\n" + getString(R.string.about_credits);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_title)
                .setMessage(message)
                .setPositiveButton(R.string.close, null)
                .show();
    }
}
