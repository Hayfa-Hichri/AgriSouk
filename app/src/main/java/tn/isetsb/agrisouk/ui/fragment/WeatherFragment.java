package tn.isetsb.agrisouk.ui.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Locale;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.api.WeatherApi;
import tn.isetsb.agrisouk.model.Weather;
import tn.isetsb.agrisouk.ui.MainActivity;
import tn.isetsb.agrisouk.ui.adapter.WeatherAdapter;
import tn.isetsb.agrisouk.util.DailyAdvice;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.WeatherCodes;

/**
 * Météo agricole : conditions actuelles, prévisions 5 jours et conseil du jour (Open-Meteo).
 */
public class WeatherFragment extends Fragment {

    private SwipeRefreshLayout swipeWeather;
    private WeatherAdapter adapter;

    private ImageView ivCurrentIcon;
    private ImageView ivAdviceIcon;
    private TextView tvCurrentTemp;
    private TextView tvCurrentCondition;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvWeatherTime;
    private TextView tvWeatherCache;
    private TextView tvAdviceTitle;
    private TextView tvWeatherAdvice;
    private TextView tvWeatherError;
    private View weatherAdviceCard;
    private View weatherErrorPanel;
    private View btnListenFr;
    private View btnListenAr;
    private View btnListenEn;

    private TextToSpeech tts;
    private boolean ttsReady;
    private DailyAdvice.Verdict verdict;

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
        ivAdviceIcon = header.findViewById(R.id.ivAdviceIcon);
        tvCurrentTemp = header.findViewById(R.id.tvCurrentTemp);
        tvCurrentCondition = header.findViewById(R.id.tvCurrentCondition);
        tvHumidity = header.findViewById(R.id.tvHumidity);
        tvWind = header.findViewById(R.id.tvWind);
        tvWeatherTime = header.findViewById(R.id.tvWeatherTime);
        tvWeatherCache = header.findViewById(R.id.tvWeatherCache);
        tvAdviceTitle = header.findViewById(R.id.tvAdviceTitle);
        tvWeatherAdvice = header.findViewById(R.id.tvWeatherAdvice);
        tvWeatherError = header.findViewById(R.id.tvWeatherError);
        weatherAdviceCard = header.findViewById(R.id.weatherAdviceCard);
        weatherErrorPanel = header.findViewById(R.id.weatherErrorPanel);
        btnListenFr = header.findViewById(R.id.btnListenFr);
        btnListenAr = header.findViewById(R.id.btnListenAr);
        btnListenEn = header.findViewById(R.id.btnListenEn);

        btnListenFr.setOnClickListener(v -> speak(Locale.FRENCH));
        btnListenAr.setOnClickListener(v -> speak(Locale.forLanguageTag("ar")));
        btnListenEn.setOnClickListener(v -> speak(Locale.ENGLISH));
        setListenEnabled(false);
        initTts();

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
        if (hidden) {
            stopSpeaking();
        } else {
            loadWeather();
        }
    }

    @Override
    public void onDestroyView() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        ttsReady = false;
        verdict = null;
        super.onDestroyView();
    }

    private void initTts() {
        tts = new TextToSpeech(requireContext(), status -> ttsReady = status == TextToSpeech.SUCCESS);
    }

    private void setListenEnabled(boolean enabled) {
        btnListenFr.setEnabled(enabled);
        btnListenAr.setEnabled(enabled);
        btnListenEn.setEnabled(enabled);
    }

    private void stopSpeaking() {
        if (tts != null) {
            tts.stop();
        }
    }

    private void speak(Locale locale) {
        if (!isAdded() || verdict == null) {
            return;
        }
        if (tts == null || !ttsReady) {
            Toast.makeText(requireContext(), R.string.advice_tts_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(requireContext(), R.string.advice_voice_missing, Toast.LENGTH_LONG).show();
            return;
        }
        tts.speak(verdict.speechText(requireContext(), locale),
                TextToSpeech.QUEUE_FLUSH, null, "conseil");
    }

    private void loadWeather() {
        swipeWeather.setRefreshing(true);
        setListenEnabled(false);
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

                bindVerdict(DailyAdvice.compute(weather));
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
                verdict = null;
                setListenEnabled(false);
                tvWeatherError.setText(message);
                weatherErrorPanel.setVisibility(View.VISIBLE);
                weatherAdviceCard.setVisibility(View.GONE);
                swipeWeather.setRefreshing(false);
            }
        });
    }

    private void bindVerdict(DailyAdvice.Verdict value) {
        verdict = value;
        Context french = DailyAdvice.localized(requireContext(), Locale.FRENCH);
        tvAdviceTitle.setText(value.title(french));
        tvWeatherAdvice.setText(value.body(french));
        int color = value.level == DailyAdvice.Level.DANGER
                ? R.color.danger
                : value.level == DailyAdvice.Level.WARNING ? R.color.amber_700 : R.color.green_700;
        ivAdviceIcon.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), color)));
        setListenEnabled(true);
    }
}
