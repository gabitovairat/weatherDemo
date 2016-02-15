package com.example.weathermapexample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.example.weathermapexample.model.CityData;
import com.example.weathermapexample.model.WeaterItemsAdapter;
import com.example.weathermapexample.model.Weather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {

    private static final String TAG = "MainActivity";
    public static Activity activity;
    ListView listViewWeatherItems;
    WeaterItemsAdapter listViewWeatherItemsAdapter;

    LocationManager locationManager;
    android.location.Location currentGPSLocation;

    Button tryToAdd;
    EditText tryToAddValue;
    View progressBar;

    WeaterItemsAdapter.WeaterItemsViewContainer currentLocationViewContainer = new WeaterItemsAdapter.WeaterItemsViewContainer();

    List<CityData> allCitiesList = new ArrayList<CityData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tryToAdd = (Button)this.findViewById(R.id.tryToAdd);
        tryToAddValue = (EditText)this.findViewById(R.id.tryToAddValue);
        progressBar =  (View)this.findViewById(R.id.progressBar);

        currentLocationViewContainer.inflateFromView(this.findViewById(R.id.currentLocationViewContainer));

        initHttpCache();
        initCytiesList();

        tryToAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!utils.checkNetworkState(MainActivity.this)) {
                    utils.showNoInternetConnection();
                    return;
                }

                String cityName = tryToAddValue.getText().toString();

                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {

                        runShowProgress(true);

                        String data = ((new WeatherHttpClient()).getWeatherDataByCityName((String)params[0]));
                        if (data == null)
                        {
                            runShowProgress(false);
                            showToast(MainActivity.this.getResources().getString(R.string.defeat_add_city));
                            return null;
                        }

                        try {
                            Weather weather = JSONWeatherParser.getWeather(data);
                            final String realCityName = weather.location.getCity();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showConfirmAddCity(realCityName);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runShowProgress(false);
                        return null;
                    }
                };
                task.execute(cityName);
            }
        });

        listViewWeatherItems = (ListView)this.findViewById(R.id.listViewWeatherItems);
        listViewWeatherItemsAdapter = new WeaterItemsAdapter(this);
        listViewWeatherItems.setAdapter(listViewWeatherItemsAdapter);
        activity = this;

        listViewWeatherItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                showConfirmForDeleteItem(position);
                view.setSelected(true);
            }
        });

        locationInit();

        //the city list is too long(200 000 cities!)
        //initCityList();
    }

    void showConfirmAddCity(final String cityName)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.add_city_confirmation) + " " + cityName + "?");

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                allCitiesList.add(new CityData("", cityName));
                saveCityListToPref();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tryToAddValue.setText("");
                        showToast(MainActivity.this.getResources().getString(R.string.succes_add_city));
                        updateData();
                    }
                });
            }
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.CANCEL), null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showConfirmForDeleteItem(final int position)
    {
        final Weather itemData = (Weather)listViewWeatherItemsAdapter.getItem(position);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.remove_city_confirmation) + " " +itemData.location.getCity() + "?");

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (listViewWeatherItemsAdapter.getCount() == allCitiesList.size())
                {
                    allCitiesList.remove(position);
                }
                else
                {
                    allCitiesList.remove(position-1);
                }
                saveCityListToPref();
                updateData();
            }
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.CANCEL), null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showToast(String text)
    {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    void runShowProgress(final boolean show)
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    void locationInit()
    {
        // Getting LocationManager object
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        String provider = locationManager.getBestProvider(criteria, false);

        if (provider!=null && !provider.equals("")){

            // Get the location from the given provider
            android.location.Location location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 20000, 1, this);

            if(location!=null)
                onLocationChanged(location);
            else
                Toast.makeText(getBaseContext(), "Location can't be retrieved", Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        updateData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    void initHttpCache()
    {
        try {
            File httpCacheDir = new File(this.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        }
        catch (IOException e) {
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }
    }

    void offHttpCache()
    {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        offHttpCache();
    }

    public void updateData()
    {
        if (!utils.checkNetworkState(this))
            utils.showNoInternetConnection();

        {
            JSONWeatherTask task = new JSONWeatherTask();
            task.execute(new String[]{});
        }
    }

    public void saveCityListToPref()
    {
        SharedPreferences sp = getSharedPreferences("SHARED_PREFS_FILE", Context.MODE_PRIVATE);

        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("cityListSize", allCitiesList.size()); /* sKey is an array */

        for(int i=0;i<allCitiesList.size();i++)
        {
            mEdit1.remove("City_" + i);
            mEdit1.putString("City_" + i, allCitiesList.get(i).displayName);
        }

        mEdit1.commit();
    }

    public void loadCityListFromPref()
    {
        SharedPreferences mSharedPreference1 = getSharedPreferences("SHARED_PREFS_FILE", Context.MODE_PRIVATE);

        allCitiesList.clear();
        int size = mSharedPreference1.getInt("cityListSize", 0);

        for(int i=0;i<size;i++)
        {
            allCitiesList.add(new CityData("", mSharedPreference1.getString("City_" + i, null)));
        }
    }

    void initCytiesList()
    {
        loadCityListFromPref();

        if (allCitiesList.isEmpty())
        {
            allCitiesList.add(new CityData("", "London,UK"));
            allCitiesList.add(new CityData("", "Toki,JP"));
            allCitiesList.add(new CityData("", "New York,US"));
        }
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        if (currentGPSLocation == null)
        {
            currentGPSLocation = location;
            updateData();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, ArrayList<Weather>> {

        @Override
        protected ArrayList<Weather> doInBackground(String... params) {

            ArrayList<Weather> result = new ArrayList<Weather>();

            String currentLocationData = ( (new WeatherHttpClient()).getWeatherDataByGPSLocation(currentGPSLocation.getLatitude(), currentGPSLocation.getLongitude()));
            if (currentLocationData!=null) {
                try {
                    final Weather weatherCurrentLocation = JSONWeatherParser.getWeather(currentLocationData);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentLocationViewContainer.updateByWeater(weatherCurrentLocation);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            for (CityData cityData : allCitiesList) {

                String data = ((new WeatherHttpClient()).getWeatherDataByCityName(cityData.displayName));
                if (data == null)
                    continue;

                try {
                    Weather weather = JSONWeatherParser.getWeather(data);
                    result.add(weather);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute( ArrayList<Weather> weatherList) {
            super.onPostExecute(weatherList);
            listViewWeatherItemsAdapter.setData(weatherList);
            listViewWeatherItemsAdapter.notifyDataSetChanged();
            listViewWeatherItems.invalidateViews();
        }

    }

}