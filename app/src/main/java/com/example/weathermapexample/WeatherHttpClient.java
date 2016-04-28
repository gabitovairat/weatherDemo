package com.example.weathermapexample;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherHttpClient {

    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static String IMG_URL = "http://openweathermap.org/img/w/";

    private static String FORE_CAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

    //by current coords
    //api.openweathermap.org/data/2.5/weather?lat=35&lon=139

    //for several cities
    //http://api.openweathermap.org/data/2.5/group?id=524901,703448,2643743&units=metric

    int maxStale = 60 * 60 * 24; // 1 day

    private static String APPID = "23b97ddf2def786de6361affbc20384d";

    public String getWeatherDataByCityName(String location)
    {
        String url = BASE_URL + "q=" + "\"" + prepareUrlParams(location) + "\"" + "&APPID="+APPID;
        return httpRequest(url);
    }

    public String getWeatherDataByGPSLocation(double lat, double lon)
    {
        String url = BASE_URL + "lat=" + lat + "&lon="+ lon +"&APPID="+APPID + "&lang=" +"ru";
        return httpRequest(url);
    }

    String prepareUrlParams(String source){
        try {
            return URLEncoder.encode(source, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return source;
        }
    }

    public String httpRequest(String url) {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            InputStream cached = null;
            con = (HttpURLConnection) ( new URL(url)).openConnection();
            con.addRequestProperty("Cache-Control", "max-stale=" + maxStale);
            try
            {
                cached = con.getInputStream();
            }
            catch (FileNotFoundException e) {
                // the resource was not cached
            }
            if (cached == null) {
                con.setRequestMethod("POST");
                con.setUseCaches(true);
                con.setDoInput(true);
                con.setDoOutput(true);
                con.connect();
                is = con.getInputStream();
            }
            else
            {
                is = cached;
            }

            // Let's read the response
            StringBuffer buffer = new StringBuffer();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }

    //not working yet
    public byte[] getImage(String code) {
        HttpURLConnection con = null ;
        InputStream is = null;
        try {
            String url = IMG_URL + code + ".png";// + "&APPID="+APPID;

            con = (HttpURLConnection) ( new URL(url)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            is = con.getInputStream();
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ( is.read(buffer) != -1)
                baos.write(buffer);

            return baos.toByteArray();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }
}
