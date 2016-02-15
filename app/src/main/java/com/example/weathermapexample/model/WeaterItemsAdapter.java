package com.example.weathermapexample.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.weathermapexample.R;

import java.util.ArrayList;

/**
 * Created by Айрат on 14.02.2016.
 */
public class WeaterItemsAdapter extends BaseAdapter {

    static public class WeaterItemsViewContainer
    {
        public TextView cityText;
        public TextView condDescr;
        public TextView temp;
        public TextView press;
        public TextView windSpeed;
        public TextView windDeg;
        public TextView hum;
        public ImageView imgView;

        public void updateByWeater(Weather weather)
        {
            if (weather.iconData != null && weather.iconData.length > 0)
            {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }

            if (cityText == null)
                return;

            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "°C");
            hum.setText("" + weather.currentCondition.getHumidity() + "%");
            press.setText("" + weather.currentCondition.getPressure() + " hPa");
            windSpeed.setText("" + weather.wind.getSpeed() + " mps");
            windDeg.setText("" + weather.wind.getDeg() + "�");
        }

        public void inflateFromView(View convertView) {
            cityText = (TextView) convertView.findViewById(R.id.cityText);
            condDescr = (TextView) convertView.findViewById(R.id.condDescr);
            temp = (TextView) convertView.findViewById(R.id.temp);
            hum = (TextView) convertView.findViewById(R.id.hum);
            press = (TextView) convertView.findViewById(R.id.press);
            windSpeed = (TextView) convertView.findViewById(R.id.windSpeed);
            windDeg = (TextView) convertView.findViewById(R.id.windDeg);
            imgView = (ImageView) convertView.findViewById(R.id.condIcon);
        }
    };

    Context context;
    ArrayList<Weather> data = new ArrayList<Weather>();

    public WeaterItemsAdapter(Context context)
    {
        this.context = context;
    }

    public void setData(ArrayList<Weather> data)
    {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WeaterItemsViewContainer itemsViewContainer = null;

        if (convertView != null && convertView.getTag() != null)
        {
            itemsViewContainer = (WeaterItemsViewContainer)convertView.getTag();
        }

        if (itemsViewContainer == null) {
            convertView = View.inflate(context, R.layout.item_view, null);

            itemsViewContainer = new WeaterItemsViewContainer();
            itemsViewContainer.inflateFromView(convertView);
            convertView.setTag(itemsViewContainer);
        }

        Weather weather = data.get(position);
        itemsViewContainer.updateByWeater(weather);

        return convertView;
    }
}
