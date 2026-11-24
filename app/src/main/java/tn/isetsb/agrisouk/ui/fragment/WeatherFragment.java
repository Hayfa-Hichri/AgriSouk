package tn.isetsb.agrisouk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.api.WeatherApi;
import tn.isetsb.agrisouk.model.Weather;
import tn.isetsb.agrisouk.ui.MainActivity;
import tn.isetsb.agrisouk.ui.adapter.WeatherAdapter;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.WeatherCodes;

/**
 * Météo agricole : conditions actuelles et prévisions sur 5 jours (Open-Meteo).
 */
public class WeatherFragment extends Fragment {

    private SwipeRefreshLayout swipeWeather;
    private WeatherAdapter adapter;

    private ImageView ivCurrentIcon;
    private TextView tvCurrentTemp;
    private TextView tvCurrentCondition;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvWeatherTime;
    private TextView tvWeatherCache;
    private TextView tvWeatherAdvice;
    private TextView tvWeatherError;
    private View weatherAdviceCard;
    private View weatherErrorPanel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity main = (MainActivity) requireActivity();
        ListView listForecast = view.findViewById(R.id.listForecast);
        swipeWeather = view.findViewById(R.id.swipeWeather);
        swipeWeather.setColorSchemeResources(R.color.green_700);

        View header = getLayoutInflater().inflate(R.layout.header_weather, listForecast, false);
        View footer = getLayoutInflater().inflate(R.layout.footer_weather, listForecast, false);
        listForecast.addHeaderView(header);
        listForecast.addFooterView(footer);

        ivCurrentIcon = header.findViewById(R.id.ivCurrentIcon);
        tvCurrentTemp = header.findViewById(R.id.tvCurrentTemp);
        tvCurrentCondition = header.findViewById(R.id.tvCurrentCondition);
        tvHumidity = header.findViewById(R.id.tvHumidity);
        tvWind = header.findViewById(R.id.tvWind);
        tvWeatherTime = header.findViewById(R.id.tvWeatherTime);
        tvWeatherCache = header.findViewById(R.id.tvWeatherCache);
        tvWeatherAdvice = header.findViewById(R.id.tvWeatherAdvice);
        tvWeatherError = header.findViewById(R.id.tvWeatherError);
        weatherAdviceCard = header.findViewById(R.id.weatherAdviceCard);
        weatherErrorPanel = header.findViewById(R.id.weatherErrorPanel);

        header.findViewById(R.id.btnWeatherRefresh).setOnClickListener(v -> loadWeather());
        header.findViewById(R.id.btnWeatherRetry).setOnClickListener(v -> loadWeather());
        footer.findViewById(R.id.btnWeatherBulletin).setOnClickListener(v -> main.openWebView(false));

        adapter = new WeatherAdapter(requireContext(), new ArrayList<>());
        listForecast.setAdapter(adapter);

        swipeWeather.setOnRefreshListener(this::loadWeather);
        loadWeather();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadWeather();
        }
    }

    private void loadWeather() {
        swipeWeather.setRefreshing(true);
        WeatherApi.fetch(requireContext(), new WeatherApi.Callback() {
            @Override
            public void onSuccess(Weather weather, boolean fromCache) {
                if (!isAdded()) {
                    return;
                }
                ivCurrentIcon.setImageResource(WeatherCodes.icon(weather.getCode()));
                tvCurrentTemp.setText(getString(R.string.weather_temperature,
                        FormatUtils.temperature(weather.getTemperature())));
                tvCurrentCondition.setText(WeatherCodes.label(weather.getCode()));
                tvHumidity.setText(getString(R.string.weather_humidity,
                        FormatUtils.quantity(weather.getHumidity())));
                tvWind.setText(getString(R.string.weather_wind,
                        FormatUtils.quantity(weather.getWindSpeed())));
                tvWeatherTime.setText(fromCache
                        ? getString(R.string.weather_cached, weather.getTimeLabel())
                        : getString(R.string.weather_updated, weather.getTimeLabel()));
                tvWeatherCache.setVisibility(fromCache ? View.VISIBLE : View.GONE);

                tvWeatherAdvice.setText(WeatherCodes.advice(weather.getCode()));
                weatherAdviceCard.setVisibility(View.VISIBLE);
                weatherErrorPanel.setVisibility(View.GONE);

                adapter.setItems(weather.getDays());
                swipeWeather.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                tvWeatherError.setText(message);
                weatherErrorPanel.setVisibility(View.VISIBLE);
                weatherAdviceCard.setVisibility(View.GONE);
                swipeWeather.setRefreshing(false);
            }
        });
    }
}
