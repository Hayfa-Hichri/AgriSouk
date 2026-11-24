package tn.isetsb.agrisouk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.ui.fragment.FavoritesDialog;
import tn.isetsb.agrisouk.ui.fragment.HomeFragment;
import tn.isetsb.agrisouk.ui.fragment.MarketFragment;
import tn.isetsb.agrisouk.ui.fragment.ProfileFragment;
import tn.isetsb.agrisouk.ui.fragment.WeatherFragment;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Coque principale : barre d'outils, navigation basse et fragments conservés en mémoire.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "home";
    private static final String TAG_MARKET = "market";
    private static final String TAG_WEATHER = "weather";
    private static final String TAG_PROFILE = "profile";
    private static final String STATE_TAG = "current_tag";

    private SessionManager session;
    private BottomNavigationView bottomNav;
    private String currentTag = TAG_HOME;
    private boolean syncingSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            openLogin();
            return;
        }
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (syncingSelection) {
                return true;
            }
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showTab(TAG_HOME, R.id.nav_home);
            } else if (id == R.id.nav_market) {
                showTab(TAG_MARKET, R.id.nav_market);
            } else if (id == R.id.nav_weather) {
                showTab(TAG_WEATHER, R.id.nav_weather);
            } else if (id == R.id.nav_profile) {
                showTab(TAG_PROFILE, R.id.nav_profile);
            }
            return true;
        });

        if (savedInstanceState == null) {
            showTab(TAG_HOME, R.id.nav_home);
        } else {
            currentTag = savedInstanceState.getString(STATE_TAG, TAG_HOME);
            showTab(currentTag, menuIdFor(currentTag));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!TAG_HOME.equals(currentTag)) {
                    openTab(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_TAG, currentTag);
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_bulletin) {
            openWebView(false);
            return true;
        }
        if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        return false;
    }

    private void showTab(String tag, int menuId) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment target = manager.findFragmentByTag(tag);
        FragmentTransaction transaction = manager.beginTransaction();
        if (target == null) {
            target = createFragment(tag);
            transaction.add(R.id.fragmentContainer, target, tag);
        }
        for (Fragment fragment : manager.getFragments()) {
            if (fragment != target && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        transaction.show(target);
        transaction.commitNow();
        currentTag = tag;

        syncingSelection = true;
        bottomNav.setSelectedItemId(menuId);
        syncingSelection = false;
    }

    private Fragment createFragment(String tag) {
        if (TAG_MARKET.equals(tag)) {
            return new MarketFragment();
        }
        if (TAG_WEATHER.equals(tag)) {
            return new WeatherFragment();
        }
        if (TAG_PROFILE.equals(tag)) {
            return new ProfileFragment();
        }
        return new HomeFragment();
    }

    private int menuIdFor(String tag) {
        if (TAG_MARKET.equals(tag)) {
            return R.id.nav_market;
        }
        if (TAG_WEATHER.equals(tag)) {
            return R.id.nav_weather;
        }
        if (TAG_PROFILE.equals(tag)) {
            return R.id.nav_profile;
        }
        return R.id.nav_home;
    }

    public void openTab(int menuId) {
        if (bottomNav.getSelectedItemId() == menuId) {
            showTab(tagFor(menuId), menuId);
        } else {
            bottomNav.setSelectedItemId(menuId);
        }
    }

    private String tagFor(int menuId) {
        if (menuId == R.id.nav_market) {
            return TAG_MARKET;
        }
        if (menuId == R.id.nav_weather) {
            return TAG_WEATHER;
        }
        if (menuId == R.id.nav_profile) {
            return TAG_PROFILE;
        }
        return TAG_HOME;
    }

    public void openMarket(String category) {
        openTab(R.id.nav_market);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_MARKET);
        if (fragment instanceof MarketFragment) {
            ((MarketFragment) fragment).setCategory(category);
        }
    }

    public void openProduct(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    public void openAdmin() {
        if (!session.isAdmin()) {
            Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, AdminActivity.class));
    }

    public void openWebView(boolean remote) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_REMOTE, remote);
        startActivity(intent);
    }

    public void showFavorites() {
        new FavoritesDialog().show(getSupportFragmentManager(), "favorites");
    }

    public void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        session.logout();
        openLogin();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
