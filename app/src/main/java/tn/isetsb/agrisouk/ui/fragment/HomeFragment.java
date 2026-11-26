package tn.isetsb.agrisouk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.api.WeatherApi;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.Weather;
import tn.isetsb.agrisouk.ui.MainActivity;
import tn.isetsb.agrisouk.ui.adapter.CategoryAdapter;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.MapsLauncher;
import tn.isetsb.agrisouk.util.SessionManager;
import tn.isetsb.agrisouk.util.WeatherCodes;

/**
 * Tableau de bord : résumé du marché, météo du jour, accès rapides.
 */
public class HomeFragment extends Fragment {

    private SessionManager session;
    private ProductRepository repository;
    private CategoryAdapter categoryAdapter;

    private TextView tvStatProducts;
    private TextView tvStatCategories;
    private TextView tvStatFavorites;
    private TextView tvHomeWeatherTemp;
    private TextView tvHomeWeatherCondition;
    private TextView tvHomeWeatherHumidity;
    private TextView tvHomeWeatherWind;
    private TextView tvHomeWeatherTime;
    private ImageView ivHomeWeatherIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        repository = new ProductRepository(requireContext());
        MainActivity main = (MainActivity) requireActivity();

        ((TextView) view.findViewById(R.id.tvHomeDate))
                .setText(getString(R.string.home_date, FormatUtils.today()));
        ((TextView) view.findViewById(R.id.tvHomeGreeting))
                .setText(getString(R.string.home_greeting, session.getFirstName()));

        TextView role = view.findViewById(R.id.tvHomeRole);
        boolean admin = session.isAdmin();
        role.setText(admin ? R.string.role_admin : R.string.role_user);
        role.setBackgroundResource(admin ? R.drawable.bg_chip_admin : R.drawable.bg_circle_glass);
        role.setTextColor(ContextCompat.getColor(requireContext(),
                admin ? R.color.amber_700 : R.color.white));

        tvStatProducts = view.findViewById(R.id.tvStatProducts);
        tvStatCategories = view.findViewById(R.id.tvStatCategories);
        tvStatFavorites = view.findViewById(R.id.tvStatFavorites);
        tvHomeWeatherTemp = view.findViewById(R.id.tvHomeWeatherTemp);
        tvHomeWeatherCondition = view.findViewById(R.id.tvHomeWeatherCondition);
        tvHomeWeatherHumidity = view.findViewById(R.id.tvHomeWeatherHumidity);
        tvHomeWeatherWind = view.findViewById(R.id.tvHomeWeatherWind);
        tvHomeWeatherTime = view.findViewById(R.id.tvHomeWeatherTime);
        ivHomeWeatherIcon = view.findViewById(R.id.ivHomeWeatherIcon);

        GridView gridCategories = view.findViewById(R.id.gridCategories);
        categoryAdapter = new CategoryAdapter(requireContext(), new ArrayList<>(),
                category -> main.openMarket(category));
        gridCategories.setAdapter(categoryAdapter);

        view.findViewById(R.id.btnSeeAll).setOnClickListener(v -> main.openTab(R.id.nav_market));
        view.findViewById(R.id.cardMarket).setOnClickListener(v -> main.openTab(R.id.nav_market));
        view.findViewById(R.id.cardWeather).setOnClickListener(v -> main.openTab(R.id.nav_weather));
        view.findViewById(R.id.cardBulletin).setOnClickListener(v -> main.openWebView(false));
        view.findViewById(R.id.cardItinerary).setOnClickListener(v -> MapsLauncher.openMarket(requireContext()));
        view.findViewById(R.id.cardHomeWeather).setOnClickListener(v -> main.openTab(R.id.nav_weather));
        ((ImageButton) view.findViewById(R.id.btnHomeWeatherRefresh))
                .setOnClickListener(v -> loadWeather());

        View cardManage = view.findViewById(R.id.cardManage);
        ImageView quickIcon = view.findViewById(R.id.ivQuickManageIcon);
        TextView quickTitle = view.findViewById(R.id.tvQuickManageTitle);
        TextView quickSub = view.findViewById(R.id.tvQuickManageSub);
        if (admin) {
            quickIcon.setImageResource(R.drawable.ic_dashboard);
            quickTitle.setText(R.string.quick_admin);
            quickSub.setText(R.string.quick_admin_sub);
            cardManage.setOnClickListener(v -> main.openAdmin());
        } else {
            quickIcon.setImageResource(R.drawable.ic_favorite);
            quickTitle.setText(R.string.quick_favorites);
            quickSub.setText(R.string.quick_favorites_sub);
            cardManage.setOnClickListener(v -> main.showFavorites());
        }

        loadStats();
        loadCategories();
        loadWeather();
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

    private void loadStats() {
        tvStatProducts.setText(String.valueOf(repository.countProducts()));
        tvStatCategories.setText(String.valueOf(repository.countCategories()));
        tvStatFavorites.setText(String.valueOf(repository.countFavorites(session.getUserId())));
    }

    private void loadCategories() {
        List<CategoryAdapter.Item> items = new ArrayList<>();
        for (String category : repository.getCategories()) {
            items.add(new CategoryAdapter.Item(category, repository.countByCategory(category)));
        }
        categoryAdapter.setItems(items);
    }

    private void loadWeather() {
        if (tvHomeWeatherCondition != null) {
            tvHomeWeatherCondition.setText(R.string.weather_loading);
        }
        WeatherApi.fetch(requireContext(), new WeatherApi.Callback() {
            @Override
            public void onSuccess(Weather weather, boolean fromCache) {
                if (!isAdded()) {
                    return;
                }
                ivHomeWeatherIcon.setImageResource(WeatherCodes.icon(weather.getCode()));
                tvHomeWeatherTemp.setText(getString(R.string.weather_temperature,
                        FormatUtils.temperature(weather.getTemperature())));
                tvHomeWeatherCondition.setText(WeatherCodes.label(weather.getCode()));
                tvHomeWeatherHumidity.setText(getString(R.string.weather_humidity,
                        FormatUtils.quantity(weather.getHumidity())));
                tvHomeWeatherWind.setText(getString(R.string.weather_wind,
                        FormatUtils.quantity(weather.getWindSpeed())));
                tvHomeWeatherTime.setText(fromCache
                        ? getString(R.string.weather_cached, weather.getTimeLabel())
                        : getString(R.string.weather_updated, weather.getTimeLabel()));
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                ivHomeWeatherIcon.setImageResource(R.drawable.ic_cloudy);
                tvHomeWeatherTemp.setText("—");
                tvHomeWeatherCondition.setText(R.string.weather_unavailable);
                tvHomeWeatherHumidity.setText("—");
                tvHomeWeatherWind.setText("—");
                tvHomeWeatherTime.setText("");
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
