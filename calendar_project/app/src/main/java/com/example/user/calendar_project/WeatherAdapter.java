package com.example.user.calendar_project;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/*
Lukas Newman
WeatherAdapter.java
Midterm
Emulated on API 25
 */

public class WeatherAdapter extends ArrayAdapter<Forecast> {
    Context context;
    ViewHolder viewHolder = null;
    int resource = 0;

    public WeatherAdapter(Context context, int resource, List<Forecast> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Forecast fore = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(this.resource, parent, false);
            viewHolder  = new ViewHolder();
            convertView.setTag(viewHolder);

            viewHolder.date = (TextView)convertView.findViewById(R.id.textViewDate);
            viewHolder.description = (TextView)convertView.findViewById(R.id.textViewWeather);
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.imageView);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.date.setText(fore.getDatetimeText());
        viewHolder.description.setText(fore.getDescription());
        Picasso.get().load(fore.getIconURL()).into(viewHolder.icon);

        return convertView;
    }

    private class ViewHolder {
        TextView date;
        TextView description;
        ImageView icon;
    }

}
