package tn.isetsb.agrisouk.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.WeatherDay;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.WeatherCodes;

/**
 * Liste des prévisions météo (5 jours) issue de l'API Open-Meteo.
 */
public class WeatherAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private List<WeatherDay> items = new ArrayList<>();

    public WeatherAdapter(Context context, List<WeatherDay> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items == null ? new ArrayList<>() : items;
    }

    public void setItems(List<WeatherDay> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public WeatherDay getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_weather_day, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WeatherDay day = getItem(position);
        holder.dayName.setText(FormatUtils.dayNameIso(day.getIsoDate())
                + " " + FormatUtils.dayMonthIso(day.getIsoDate()));
        holder.condition.setText(WeatherCodes.label(day.getCode()));
        holder.icon.setImageResource(WeatherCodes.icon(day.getCode()));
        holder.max.setText(context.getString(R.string.weather_temperature,
                FormatUtils.temperature(day.getMaxTemperature())));
        holder.min.setText(context.getString(R.string.weather_temperature,
                FormatUtils.temperature(day.getMinTemperature())));
        return convertView;
    }

    private static final class ViewHolder {
        private final ImageView icon;
        private final TextView dayName;
        private final TextView condition;
        private final TextView max;
        private final TextView min;

        private ViewHolder(View view) {
            icon = view.findViewById(R.id.ivDayIcon);
            dayName = view.findViewById(R.id.tvDayName);
            condition = view.findViewById(R.id.tvDayCondition);
            max = view.findViewById(R.id.tvDayMax);
            min = view.findViewById(R.id.tvDayMin);
        }
    }
}
